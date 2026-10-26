package konser.service;

import konser.model.Concert;

import konser.model.Ticket;

import konser.repository.ConcertRepository;

import java.math.BigDecimal;

import java.sql.SQLException;

import java.time.LocalDateTime;

import java.util.List;

public class ConcertService {

    private final ConcertRepository concerts;

    public ConcertService(ConcertRepository concerts) {
        this.concerts=concerts;
    }

public List<Concert> getConcerts() throws SQLException {
    return concerts.findAll();
}

public List<Ticket> getTickets() throws SQLException {
    return concerts.findTickets();
}

public void addConcert(String name,String city,String venue,LocalDateTime start,LocalDateTime end,BigDecimal premium,int premiumQuota,BigDecimal normal,int normalQuota) throws SQLException {

    if(name==null||name.isBlank()||city==null||city.isBlank()||venue==null||venue.isBlank()) throw new IllegalArgumentException("Nama konser, kota, dan stadion wajib diisi.");

    if(premium.signum()<0||normal.signum()<0||premiumQuota<0||normalQuota<0) throw new IllegalArgumentException("Harga dan kuota tidak boleh negatif.");

    concerts.addConcert(name.trim(),city.trim(),venue.trim(),start,end,premium,premiumQuota,normal,normalQuota);

}

}
