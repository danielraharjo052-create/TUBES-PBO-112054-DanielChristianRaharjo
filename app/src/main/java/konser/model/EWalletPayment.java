package konser.model;

import java.security.SecureRandom;

public class EWalletPayment implements PaymentStrategy {

    @Override public String getMethodName() {
        return "EWALLET - QR SIMULASI";
    }

@Override public String createPaymentReference() {

    SecureRandom random = new SecureRandom();

    return "QR-SIM-" + (100000 + random.nextInt(900000));

}

}
