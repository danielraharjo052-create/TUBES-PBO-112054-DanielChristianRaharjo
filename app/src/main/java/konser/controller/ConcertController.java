package konser.controller;

import konser.model.*;

import konser.service.ConcertService;

import java.math.BigDecimal;

import java.sql.SQLException;

import java.time.LocalDateTime;

import java.util.List;

public class ConcertController {

    private final ConcertService service;

    public ConcertController(ConcertService service) {
        this.service=service;
    }

public List<Concert> concerts() throws SQLException {
    return service.getConcerts();
}

public List<Ticket> tickets() throws SQLException {
    return service.getTickets();
}

public void addConcert(User user,String name,String city,String venue,LocalDateTime start,LocalDateTime end,BigDecimal premium,int premiumQuota,BigDecimal normal,int normalQuota) throws SQLException {

    if(!"ADMIN".equals(user.getRole())) throw new SecurityExceptionUnchecked("Akses ditolak: hanya ADMIN yang dapat menambah konser.");

    service.addConcert(name,city,venue,start,end,premium,premiumQuota,normal,normalQuota);

}

public void deleteConcert(User user, int id) throws SQLException {
    if (user == null || !"ADMIN".equals(user.getRole()))
        throw new SecurityExceptionUnchecked("Akses ditolak: hanya ADMIN yang dapat menghapus konser.");
    service.deleteConcert(id);
}

public static class SecurityExceptionUnchecked extends RuntimeException {
    public SecurityExceptionUnchecked(String message){
        super(message);
    }
}

}
