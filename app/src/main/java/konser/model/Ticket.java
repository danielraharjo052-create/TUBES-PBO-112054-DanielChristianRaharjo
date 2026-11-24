package konser.model;

import java.math.BigDecimal;

public class Ticket extends Entity {

    private final int concertId;

    private final String category;

    private final BigDecimal price;

    private final int quota;

    private final String concertName;

    public Ticket(int id, int concertId, String category, BigDecimal price, int quota, String concertName) {

        super(id);
        this.concertId = concertId;
        this.category = category;
        this.price = price;
        this.quota = quota;
        this.concertName = concertName;

    }

public int getConcertId() {
    return concertId;
}

public String getCategory() {
    return category;
}

public BigDecimal getPrice() {
    return price;
}

public int getQuota() {
    return quota;
}

public String getConcertName() {
    return concertName;
}

@Override public String toString() {
    return id + " | " + concertName + " | " + category + " | Rp" + price.toPlainString() + " | Kuota: " + quota;
}

}
