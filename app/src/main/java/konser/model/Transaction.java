package konser.model;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public class Transaction extends Entity {

    private final int userId;

    private final int ticketId;

    private final int quantity;

    private final BigDecimal totalPrice;

    private final String paymentMethod;

    private final String paymentReference;

    private final String status;

    private final LocalDateTime createdAt;

    private final String ticketLabel;

    public Transaction(int id, int userId, int ticketId, int quantity, BigDecimal totalPrice, String paymentMethod,
    String paymentReference, String status, LocalDateTime createdAt, String ticketLabel) {

        super(id);
        this.userId = userId;
        this.ticketId = ticketId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;

        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        this.status = status;

        this.createdAt = createdAt;
        this.ticketLabel = ticketLabel;

    }

public int getUserId() {
    return userId;
}

public int getTicketId() {
    return ticketId;
}

public int getQuantity() {
    return quantity;
}

public BigDecimal getTotalPrice() {
    return totalPrice;
}

public String getPaymentMethod() {
    return paymentMethod;
}

public String getPaymentReference() {
    return paymentReference;
}

public String getStatus() {
    return status;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

@Override public String toString() {
    return "#" + id + " | " + ticketLabel + " | " + quantity + " tiket | Rp" + totalPrice.toPlainString() + " | " + paymentMethod + " | " + status + (paymentReference == null ? "" : " | " + paymentReference);
}

}
