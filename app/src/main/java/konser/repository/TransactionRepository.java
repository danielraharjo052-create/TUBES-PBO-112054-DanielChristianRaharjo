package konser.repository;

import konser.config.DatabaseConnection;
import konser.exception.TicketUnavailableException;
import konser.model.Ticket;
import konser.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final DatabaseConnection db;

    public TransactionRepository(DatabaseConnection db) {
        this.db = db;
    }

    public Transaction book(int userId, Ticket ticket, int quantity, String method, String reference)
            throws SQLException, TicketUnavailableException {
        if (quantity <= 0) throw new IllegalArgumentException("Jumlah tiket harus lebih dari nol.");
        expirePendingTransactions();

        String lockSql = "SELECT quota, price FROM tickets WHERE id=? FOR UPDATE";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int quota;
                BigDecimal price;
                try (PreparedStatement ps = c.prepareStatement(lockSql)) {
                    ps.setInt(1, ticket.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new TicketUnavailableException("Data tiket tidak ditemukan.");
                        quota = rs.getInt("quota");
                        price = rs.getBigDecimal("price");
                    }
                }
                if (quantity > quota)
                    throw new TicketUnavailableException("Kuota tiket tidak mencukupi. Sisa kuota: " + quota);

                BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
                try (PreparedStatement ps = c.prepareStatement("UPDATE tickets SET quota=quota-? WHERE id=?")) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, ticket.getId());
                    ps.executeUpdate();
                }

                String sql = "INSERT INTO transactions(user_id,ticket_id,quantity,total_price,payment_method,payment_reference,status) " +
                        "VALUES(?,?,?,?,?,?,'PENDING') RETURNING id,created_at";
                int transactionId;
                java.time.LocalDateTime created;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, ticket.getId());
                    ps.setInt(3, quantity);
                    ps.setBigDecimal(4, total);
                    ps.setString(5, method);
                    ps.setString(6, reference);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        transactionId = rs.getInt("id");
                        created = rs.getTimestamp("created_at").toLocalDateTime();
                    }
                }
                c.commit();
                return new Transaction(transactionId, userId, ticket.getId(), quantity, total, method,
                        reference, "PENDING", created, ticket.getConcertName() + " - " + ticket.getCategory());
            } catch (SQLException | TicketUnavailableException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<Transaction> findByUser(int userId) throws SQLException {
        expirePendingTransactions();
        return findTransactions("WHERE tr.user_id=?", userId);
    }

    public List<Transaction> findAll() throws SQLException {
        expirePendingTransactions();
        return findTransactions("", null);
    }

    private List<Transaction> findTransactions(String condition, Integer userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT tr.id,tr.user_id,tr.ticket_id,tr.quantity,tr.total_price,tr.payment_method," +
                "tr.payment_reference,tr.status,tr.created_at,c.name || ' - ' || t.category AS ticket_label " +
                "FROM transactions tr JOIN tickets t ON t.id=tr.ticket_id " +
                "JOIN concerts c ON c.id=t.concert_id " + condition + " ORDER BY tr.created_at DESC, tr.id DESC";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("ticket_id"),
                            rs.getInt("quantity"), rs.getBigDecimal("total_price"), rs.getString("payment_method"),
                            rs.getString("payment_reference"), rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(), rs.getString("ticket_label")));
                }
            }
        }
        return list;
    }

    public void markAsPaid(int transactionId, int userId) throws SQLException {
        expirePendingTransactions();
        String sql = "UPDATE transactions SET status='DIBAYAR' WHERE id=? AND user_id=? AND status='PENDING' " +
                "AND created_at > CURRENT_TIMESTAMP - INTERVAL '15 minutes'";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0)
                throw new IllegalArgumentException("Transaksi tidak ditemukan, bukan milikmu, sudah diproses, atau sudah melewati batas 15 menit.");
        }
    }

    public void confirmPayment(int transactionId) throws SQLException {
        expirePendingTransactions();
        String sql = "UPDATE transactions SET status='SUKSES' WHERE id=? AND status='DIBAYAR'";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            if (ps.executeUpdate() == 0)
                throw new IllegalArgumentException("Transaksi tidak ditemukan atau customer belum menandai pembayaran.");
        }
    }

    /** Batalkan pesanan PENDING yang berumur minimal 15 menit dan kembalikan kuotanya secara atomik. */
    public int expirePendingTransactions() throws SQLException {
        String select = "SELECT id,ticket_id,quantity FROM transactions " +
                "WHERE status='PENDING' AND created_at <= CURRENT_TIMESTAMP - INTERVAL '15 minutes' " +
                "FOR UPDATE SKIP LOCKED";
        try (Connection c = db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int cancelled = 0;
                try (PreparedStatement ps = c.prepareStatement(select); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        int ticketId = rs.getInt("ticket_id");
                        int quantity = rs.getInt("quantity");
                        try (PreparedStatement update = c.prepareStatement(
                                "UPDATE transactions SET status='CANCELLED' WHERE id=? AND status='PENDING'")) {
                            update.setInt(1, id);
                            if (update.executeUpdate() == 1) {
                                try (PreparedStatement restore = c.prepareStatement(
                                        "UPDATE tickets SET quota=quota+? WHERE id=?")) {
                                    restore.setInt(1, quantity);
                                    restore.setInt(2, ticketId);
                                    restore.executeUpdate();
                                }
                                cancelled++;
                            }
                        }
                    }
                }
                c.commit();
                return cancelled;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
