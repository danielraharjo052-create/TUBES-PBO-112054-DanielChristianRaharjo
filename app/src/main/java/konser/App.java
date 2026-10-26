package konser;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import konser.config.DatabaseConnection;
import konser.controller.AuthController;
import konser.controller.ConcertController;
import konser.controller.TransactionController;
import konser.repository.ConcertRepository;
import konser.repository.TransactionRepository;
import konser.repository.UserRepository;
import konser.service.AuthService;
import konser.service.ConcertService;
import konser.service.TransactionService;
import konser.view.MainFrame;

public class App {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                DatabaseConnection db=DatabaseConnection.getInstance();
                db.testConnection();
                UserRepository userRepository=new UserRepository(db);
                ConcertRepository concertRepository=new ConcertRepository(db);
                TransactionRepository transactionRepository=new TransactionRepository(db);
                AuthService authService=new AuthService(userRepository);
                ConcertService concertService=new ConcertService(concertRepository);
                TransactionService transactionService=new TransactionService(transactionRepository);
                AuthController authController=new AuthController(authService);
                ConcertController concertController=new ConcertController(concertService);
                TransactionController transactionController=new TransactionController(transactionService);
                MainFrame frame=new MainFrame(authController,concertController,transactionController,userRepository::findAllUsers);
                frame.setVisible(true);
            }
        catch(Exception ex) {

            StartupErrorWindow.showError("Aplikasi tidak dapat dimulai. Periksa PostgreSQL dan nilai pada AppConfig.java. Detail: " + ex.getMessage());
        }

}
);

}

// Pesan error sederhana tanpa JOptionPane atau JDialog.
private static class StartupErrorWindow {

    static void showError(String message) {

        JFrame frame=new JFrame("Kesalahan aplikasi");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea area=new JTextArea(message,5,55);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);

        frame.add(new JScrollPane(area));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}

}
