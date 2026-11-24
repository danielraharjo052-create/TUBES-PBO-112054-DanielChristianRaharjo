package konser.controller;

import konser.exception.SecurityException;

import konser.exception.TicketUnavailableException;

import konser.model.*;

import konser.service.TransactionService;

import java.sql.SQLException;

import java.util.List;

public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service=service;
    }

public Transaction book(User user,Ticket ticket,int quantity,PaymentStrategy strategy) throws SQLException,TicketUnavailableException,SecurityException {
    return service.book(user,ticket,quantity,strategy);
}

public List<Transaction> history(User user) throws SQLException,SecurityException {
    return service.history(user);
}

public void confirm(User user,int id) throws SQLException, SecurityException {
    service.confirm(user,id);
}

}
