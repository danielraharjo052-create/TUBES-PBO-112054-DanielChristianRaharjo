package konser.view;

import konser.controller.*;

import konser.exception.SecurityException;

import konser.exception.TicketUnavailableException;

import konser.model.*;

import javax.swing.*;

import java.awt.*;

import java.math.BigDecimal;

import java.sql.SQLException;

import java.time.LocalDateTime;

import java.time.format.DateTimeParseException;

import java.util.List;

public class MainFrame extends JFrame {

    private final AuthController authController;

    private final ConcertController concertController;

    private final TransactionController transactionController;

    private final UserRepositoryAccess userAccess;

    private final CardLayout cardLayout = new CardLayout();

    private final JPanel cards = new JPanel(cardLayout);

    private User currentUser;

    private final JTextField username = new JTextField(18);

    private final JPasswordField password = new JPasswordField(18);

    private final JLabel loginMessage = new JLabel(" ");

    private final JLabel status = new JLabel("Siap.");

    private final DefaultListModel<String> concertListModel = new DefaultListModel<>();

    private final JList<String> concertList = new JList<>(concertListModel);

    private final DefaultListModel<Ticket> ticketListModel = new DefaultListModel<>();

    private final JList<Ticket> ticketList = new JList<>(ticketListModel);

    private final DefaultListModel<String> transactionListModel = new DefaultListModel<>();

    private final JList<String> transactionList = new JList<>(transactionListModel);

    private final JTextField concertName = new JTextField(12), city = new JTextField(10), venue = new JTextField(12);

    private final JTextField startTime = new JTextField("2027-01-15T18:00", 16), endTime = new JTextField("2027-01-15T21:00", 16);

    private final JTextField premiumPrice = new JTextField("350000", 7), premiumQuota = new JTextField("100", 5);

    private final JTextField normalPrice = new JTextField("150000", 7), normalQuota = new JTextField("300", 5);

    private final JTextField quantity = new JTextField("1", 4), transactionId = new JTextField(5);

    @FunctionalInterface public interface UserRepositoryAccess {
        List<String> allUsers() throws SQLException;
    }

public MainFrame(AuthController authController, ConcertController concertController, TransactionController transactionController, UserRepositoryAccess userAccess) {

    this.authController=authController;
    this.concertController=concertController;
    this.transactionController=transactionController;
    this.userAccess=userAccess;

    setTitle("Sistem Manajemen Tiket Konser");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1050,720);
    setLocationRelativeTo(null);

    cards.add(createLoginPanel(),"LOGIN");
    cards.add(createDashboardPanel(),"DASHBOARD");
    add(cards);

}

private JPanel createLoginPanel() {

    JPanel outer=new JPanel(new GridBagLayout());
    JPanel form=new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createTitledBorder("Login ConcertHub"));

    GridBagConstraints g=new GridBagConstraints();
    g.insets=new Insets(7,7,7,7);
    g.anchor=GridBagConstraints.WEST;

    g.gridx=0;
    g.gridy=0;
    form.add(new JLabel("Username"),g);
    g.gridx=1;
    form.add(username,g);

    g.gridx=0;
    g.gridy=1;
    form.add(new JLabel("Password"),g);
    g.gridx=1;
    form.add(password,g);

    JButton login=new JButton("Login");
    g.gridx=1;
    g.gridy=2;
    form.add(login,g);
    g.gridx=0;
    g.gridy=3;
    g.gridwidth=2;
    form.add(loginMessage,g);

    JLabel demo=new JLabel("Demo: admin/admin123 | staff/staff123 | customer/customer123");
    g.gridy=4;
    form.add(demo,g);

    login.addActionListener(e->doLogin());
    password.addActionListener(e->doLogin());
    outer.add(form);
    return outer;

}

private JPanel createDashboardPanel() {

    JPanel root=new JPanel(new BorderLayout(8,8));

    JPanel header=new JPanel(new BorderLayout());
    JLabel welcome=new JLabel("Sistem Manajemen Tiket Konser");
    welcome.setFont(welcome.getFont().deriveFont(Font.BOLD,18f));
    header.add(welcome,BorderLayout.WEST);

    JButton logout=new JButton("Logout");
    logout.addActionListener(e->{
        currentUser=null;username.setText("");password.setText("");loginMessage.setText(" ");cardLayout.show(cards,"LOGIN");}
    );
    header.add(logout,BorderLayout.EAST);
    root.add(header,BorderLayout.NORTH);

    JTabbedPane tabs=new JTabbedPane();

    JPanel concertsPanel=new JPanel(new BorderLayout(5,5));
    concertsPanel.add(new JScrollPane(concertList),BorderLayout.CENTER);

    JPanel concertActions=new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton refreshConcerts=new JButton("Muat Ulang Konser");
    refreshConcerts.addActionListener(e->loadConcerts());
    concertActions.add(refreshConcerts);
    concertsPanel.add(concertActions,BorderLayout.SOUTH);
    tabs.addTab("Konser",concertsPanel);

    JPanel ticketPanel=new JPanel(new BorderLayout(5,5));
    ticketPanel.add(new JScrollPane(ticketList),BorderLayout.CENTER);

    JPanel bookControls=new JPanel(new FlowLayout(FlowLayout.LEFT));
    bookControls.add(new JLabel("Jumlah:"));
    bookControls.add(quantity);
    JComboBox<String> method=new JComboBox<>(new String[]{
        "VA - BCA","VA - BRI","VA - BNI","E-Wallet QR"}
    );
    bookControls.add(method);
    JButton book=new JButton("Pesan Tiket");
    bookControls.add(book);
    ticketPanel.add(bookControls,BorderLayout.SOUTH);

    book.addActionListener(e->bookTicket(method.getSelectedItem().toString()));
    tabs.addTab("Pesan Tiket",ticketPanel);

    JPanel transactionPanel=new JPanel(new BorderLayout(5,5));
    transactionPanel.add(new JScrollPane(transactionListModelToList()),BorderLayout.CENTER);

    JPanel txControls=new JPanel(new FlowLayout(FlowLayout.LEFT));
    txControls.add(new JLabel("ID transaksi:"));
    txControls.add(transactionId);
    JButton refreshTx=new JButton("Muat Riwayat");
    refreshTx.addActionListener(e->loadTransactions());
    txControls.add(refreshTx);
    JButton confirm=new JButton("Konfirmasi Simulasi Bayar");
    confirm.addActionListener(e->confirmPayment());
    txControls.add(confirm);
    transactionPanel.add(txControls,BorderLayout.SOUTH);
    tabs.addTab("Transaksi",transactionPanel);

    JPanel managePanel=new JPanel(new BorderLayout(5,5));

    JPanel fields=new JPanel(new GridLayout(0,4,5,5));
    fields.setBorder(BorderFactory.createTitledBorder("Tambah konser (ADMIN/STAFF)"));

    fields.add(new JLabel("Nama konser"));
    fields.add(concertName);
    fields.add(new JLabel("Kota"));
    fields.add(city);

    fields.add(new JLabel("Stadion/Venue"));
    fields.add(venue);
    fields.add(new JLabel("Mulai (yyyy-MM-ddTHH:mm)"));
    fields.add(startTime);

    fields.add(new JLabel("Selesai (yyyy-MM-ddTHH:mm)"));
    fields.add(endTime);
    fields.add(new JLabel("Harga Premium"));
    fields.add(premiumPrice);

    fields.add(new JLabel("Kuota Premium"));
    fields.add(premiumQuota);
    fields.add(new JLabel("Harga Normal"));
    fields.add(normalPrice);

    fields.add(new JLabel("Kuota Normal"));
    fields.add(normalQuota);
    JButton addConcert=new JButton("Simpan Konser");
    fields.add(addConcert);

    addConcert.addActionListener(e->addConcert());
    managePanel.add(fields,BorderLayout.NORTH);

    JTextArea userArea=new JTextArea();
    userArea.setEditable(false);
    JButton showUsers=new JButton("Lihat Daftar Akun (ADMIN)");
    showUsers.addActionListener(e->{
        if(!isAdmin()){
            showStatus("Akses ditolak: hanya ADMIN.");return;}
        try{
            userArea.setText(String.join("\n",userAccess.allUsers()));}
        catch(SQLException ex){
            showStatus("Gagal mengambil akun: "+ex.getMessage());}
    }
);

JPanel usersPanel=new JPanel(new BorderLayout());
usersPanel.add(showUsers,BorderLayout.NORTH);
usersPanel.add(new JScrollPane(userArea),BorderLayout.CENTER);
managePanel.add(usersPanel,BorderLayout.CENTER);
tabs.addTab("Manajemen",managePanel);

root.add(tabs,BorderLayout.CENTER);
root.add(status,BorderLayout.SOUTH);

// Tab access is controlled at login; service/controller also validate permissions.
return root;

}

private JList<String> transactionListModelToList() {
    return new JList<>(transactionListModel);
}

private void doLogin() {

    try {
        currentUser=authController.login(username.getText(),new String(password.getPassword()));
        loginMessage.setText("Login berhasil sebagai "+currentUser.getRole());
        cardLayout.show(cards,"DASHBOARD");
        loadConcerts();
        loadTickets();
        loadTransactions();
        showStatus("Login berhasil: "+currentUser.getUsername()+" ("+currentUser.getRole()+")");
    }

catch(SecurityException ex){
    loginMessage.setText(ex.getMessage());
}

}

private void loadConcerts() {
    try {
        concertListModel.clear();
        for(Concert c:concertController.concerts())concertListModel.addElement(c.toString());
        showStatus("Daftar konser diperbarui.");
    }
catch(Exception ex){
    showStatus("Gagal memuat konser: "+ex.getMessage());
}
}

private void loadTickets() {
    try {
        ticketListModel.clear();
        for(Ticket t:concertController.tickets())ticketListModel.addElement(t);
    }
catch(Exception ex){
    showStatus("Gagal memuat tiket: "+ex.getMessage());
}
}

private void loadTransactions() {
    if(currentUser==null)return;
    try{
        transactionListModel.clear();
        for(Transaction t:transactionController.history(currentUser))transactionListModel.addElement(t.toString());
    }
catch(Exception ex){
    showStatus("Gagal memuat transaksi: "+ex.getMessage());
}
}

private void bookTicket(String method) {

    Ticket ticket=ticketList.getSelectedValue();
    if(ticket==null){
        showStatus("Pilih tiket dari daftar terlebih dahulu.");
        return;
    }

try {
    int count=Integer.parseInt(quantity.getText().trim());
    PaymentStrategy strategy;

    if(method.startsWith("VA")) strategy=new VAPayment(method.substring(5));
    else strategy=new EWalletPayment();

    Transaction t=transactionController.book(currentUser,ticket,count,strategy);

    if("VA".equals(t.getPaymentMethod())) showStatus("Booking dibuat. Bank "+method.substring(5)+" | VA: "+t.getPaymentReference()+" | Total Rp"+t.getTotalPrice()+" | Status PENDING");

    else showStatus("Booking dibuat. QR simulasi: "+t.getPaymentReference()+" | Total Rp"+t.getTotalPrice()+". Ini bukan QR pembayaran nyata.");

    loadTickets();
    loadTransactions();

}
catch(NumberFormatException ex){
    showStatus("Jumlah tiket harus berupa angka.");
}
catch(TicketUnavailableException ex){
    showStatus(ex.getMessage());
}
catch(SecurityException ex){
    showStatus(ex.getMessage());
}
catch(Exception ex){
    showStatus("Pemesanan gagal: "+ex.getMessage());
}

}

private void confirmPayment() {
    try{
        int id=Integer.parseInt(transactionId.getText().trim());
        transactionController.confirm(currentUser,id);
        showStatus("Pembayaran simulasi berhasil dikonfirmasi.");
        loadTransactions();
    }
catch(NumberFormatException ex){
    showStatus("Masukkan ID transaksi yang benar.");
}
catch(Exception ex){
    showStatus("Konfirmasi gagal: "+ex.getMessage());
}
}

private void addConcert() {

    if(!isStaffOrAdmin()){
        showStatus("Akses ditolak: hanya ADMIN/STAFF yang dapat menambah konser.");
        return;
    }

try {
    concertController.addConcert(currentUser,concertName.getText(),city.getText(),venue.getText(),LocalDateTime.parse(startTime.getText().trim()),LocalDateTime.parse(endTime.getText().trim()),new BigDecimal(premiumPrice.getText().trim()),Integer.parseInt(premiumQuota.getText().trim()),new BigDecimal(normalPrice.getText().trim()),Integer.parseInt(normalQuota.getText().trim()));
    showStatus("Konser berhasil ditambahkan.");
    loadConcerts();
    loadTickets();
}

catch(DateTimeParseException ex){
    showStatus("Format waktu harus yyyy-MM-ddTHH:mm, contoh 2027-01-15T18:00.");
}
catch(Exception ex){
    showStatus("Gagal menambah konser: "+ex.getMessage());
}

}

private boolean isAdmin(){
    return currentUser!=null&&"ADMIN".equals(currentUser.getRole());
}

private boolean isStaffOrAdmin(){
    return currentUser!=null&&(isAdmin()||"STAFF".equals(currentUser.getRole()));
}

private void showStatus(String message){
    status.setText(message);
}

}
