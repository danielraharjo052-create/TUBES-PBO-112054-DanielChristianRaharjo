package konser.repository;

import konser.config.DatabaseConnection;

import konser.model.Concert;

import konser.model.Ticket;

import java.math.BigDecimal;

import java.sql.*;

import java.time.LocalDateTime;

import java.util.*;

public class ConcertRepository implements Repository<Concert, Integer> {

    private final DatabaseConnection db;

    public ConcertRepository(DatabaseConnection db) {
        this.db = db;
    }

@Override public List<Concert> findAll() throws SQLException {

    List<Concert> list = new ArrayList<>();

    String sql = "SELECT id,name,city,venue,start_time,end_time FROM concerts ORDER BY start_time";

    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

        while (rs.next()) list.add(new Concert(rs.getInt("id"), rs.getString("name"), rs.getString("city"), rs.getString("venue"), rs.getTimestamp("start_time").toLocalDateTime(), rs.getTimestamp("end_time").toLocalDateTime()));

    }

return list;

}

@Override public Optional<Concert> findById(Integer id) throws SQLException {

    String sql = "SELECT id,name,city,venue,start_time,end_time FROM concerts WHERE id=?";

    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setInt(1,id);
        try (ResultSet rs=ps.executeQuery()) {
            if(rs.next()) return Optional.of(new Concert(rs.getInt("id"),rs.getString("name"),rs.getString("city"),rs.getString("venue"),rs.getTimestamp("start_time").toLocalDateTime(),rs.getTimestamp("end_time").toLocalDateTime()));
        }

}

return Optional.empty();

}

public List<Ticket> findTickets() throws SQLException {

    List<Ticket> list = new ArrayList<>();

    String sql = "SELECT t.id,t.concert_id,t.category,t.price,t.quota,c.name AS concert_name FROM tickets t JOIN concerts c ON c.id=t.concert_id ORDER BY c.start_time,t.category";

    try(Connection c=db.getConnection(); PreparedStatement ps=c.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {

        while(rs.next()) list.add(new Ticket(rs.getInt("id"),rs.getInt("concert_id"),rs.getString("category"),rs.getBigDecimal("price"),rs.getInt("quota"),rs.getString("concert_name")));

    }

return list;

}

public void addConcert(String name, String city, String venue, LocalDateTime start, LocalDateTime end, BigDecimal premium, int premiumQuota, BigDecimal normal, int normalQuota) throws SQLException {

    if (!end.isAfter(start)) throw new IllegalArgumentException("Waktu selesai harus setelah waktu mulai.");

    String overlap = "SELECT COUNT(*) FROM concerts WHERE venue=? AND start_time < ? AND end_time > ?";

    String insert = "INSERT INTO concerts(name,city,venue,start_time,end_time) VALUES(?,?,?,?,?) RETURNING id";

    try(Connection c=db.getConnection()) {

        c.setAutoCommit(false);

        try {

            try(PreparedStatement check=c.prepareStatement(overlap)) {

                check.setString(1,venue);
                check.setTimestamp(2,Timestamp.valueOf(end));
                check.setTimestamp(3,Timestamp.valueOf(start));

                try(ResultSet rs=check.executeQuery()) {
                    rs.next();
                    if(rs.getInt(1)>0) throw new IllegalArgumentException("Jadwal stadion bentrok dengan konser lain.");
                }

        }

    int concertId;

    try(PreparedStatement ps=c.prepareStatement(insert)) {

        ps.setString(1,name);
        ps.setString(2,city);
        ps.setString(3,venue);
        ps.setTimestamp(4,Timestamp.valueOf(start));
        ps.setTimestamp(5,Timestamp.valueOf(end));

        try(ResultSet rs=ps.executeQuery()) {
            rs.next();
            concertId=rs.getInt(1);
        }

}

insertTicket(c,concertId,"PREMIUM",premium,premiumQuota);
insertTicket(c,concertId,"NORMAL",normal,normalQuota);
c.commit();

}
catch(Exception e) {
    c.rollback();
    if(e instanceof SQLException se) throw se;
    if(e instanceof RuntimeException re) throw re;
    throw new SQLException(e);
}

finally {
    c.setAutoCommit(true);
}

}

}

public void deleteConcert(int concertId) throws SQLException {
    String sql = "DELETE FROM concerts WHERE id=?";
    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, concertId);
        if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Konser tidak ditemukan.");
    } catch (SQLException e) {
        if ("23503".equals(e.getSQLState()))
            throw new IllegalArgumentException("Konser tidak bisa dihapus karena sudah memiliki transaksi.");
        throw e;
    }
}

private void insertTicket(Connection c,int id,String category,BigDecimal price,int quota) throws SQLException {

    try(PreparedStatement ps=c.prepareStatement("INSERT INTO tickets(concert_id,category,price,quota) VALUES(?,?,?,?)")) {
        ps.setInt(1,id);
        ps.setString(2,category);
        ps.setBigDecimal(3,price);
        ps.setInt(4,quota);
        ps.executeUpdate();
    }

}

}
