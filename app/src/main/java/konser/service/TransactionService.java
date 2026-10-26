package konser.service;

import konser.exception.SecurityException;

import konser.exception.TicketUnavailableException;

import konser.model.*;

import konser.repository.TransactionRepository;

import java.sql.SQLException;

import java.util.List;

public class TransactionService {

    private final TransactionRepository transactions;

    public TransactionService(TransactionRepository transactions) {
        this.transactions=transactions;
    }

public Transaction book(User user, Ticket ticket, int quantity, PaymentStrategy strategy) throws SQLException, TicketUnavailableException, SecurityException {

    if(!"CUSTOMER".equals(user.getRole())) throw new SecurityException("Hanya CUSTOMER yang dapat memesan tiket.");

    String method=strategy.getMethodName().startsWith("VA") ? "VA" : "EWALLET";

    return transactions.book(user.getId(),ticket,quantity,method,strategy.createPaymentReference());

}

public List<Transaction> history(User user) throws SQLException, SecurityException {

    if("CUSTOMER".equals(user.getRole())) return transactions.findByUser(user.getId());

    if("ADMIN".equals(user.getRole())||"STAFF".equals(user.getRole())) return transactions.findAll();

    throw new SecurityException("Akses ditolak.");

}

public void confirm(User user,int transactionId) throws SQLException {

    boolean privileged="ADMIN".equals(user.getRole())||"STAFF".equals(user.getRole());

    transactions.confirmPayment(transactionId,user.getId(),privileged);

}

}
