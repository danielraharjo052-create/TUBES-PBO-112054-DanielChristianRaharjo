package konser.model;

public interface PaymentStrategy {

    String getMethodName();

    String createPaymentReference();

}
