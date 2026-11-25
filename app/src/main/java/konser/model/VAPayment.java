package konser.model;

import java.security.SecureRandom;

public class VAPayment implements PaymentStrategy {

    private final String bankName;

    public VAPayment(String bankName) {
        this.bankName = bankName;
    }

public String getBankName() {
    return bankName;
}

@Override public String getMethodName() {
    return "VA - " + bankName;
}

@Override public String createPaymentReference() {

    SecureRandom random = new SecureRandom();

    StringBuilder result = new StringBuilder("88");

    for (int i = 0; i < 13; i++) result.append(random.nextInt(10));

    return result.toString();

}

}
