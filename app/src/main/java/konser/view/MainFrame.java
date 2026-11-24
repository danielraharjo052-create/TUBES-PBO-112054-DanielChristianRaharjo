package konser.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import konser.controller.AuthController;
import konser.controller.ConcertController;
import konser.controller.TransactionController;
import konser.exception.SecurityException;
import konser.exception.TicketUnavailableException;
import konser.model.Concert;
import konser.model.EWalletPayment;
import konser.model.PaymentStrategy;
import konser.model.Ticket;
import konser.model.Transaction;
import konser.model.User;
import konser.model.VAPayment;

public class MainFrame extends JFrame {
    private static final Color NAVY = new Color(24, 43, 70);
    private static final Color BLUE = new Color(39, 72, 110);
    private static final Color GOLD = new Color(231, 177, 75);
    private static final Color BG = new Color(243, 246, 250);
    private static final Color WHITE = Color.WHITE;

    private final AuthController authController;
    private final ConcertController concertController;
    private final TransactionController transactionController;
    private final UserRepositoryAccess userAccess;
    private final StaffCreator staffCreator;

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);
    private User currentUser;

    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JLabel loginMessage = new JLabel(" ");
    private final JTextField regUsername = new JTextField(18);
    private final JPasswordField regPassword = new JPasswordField(18);
    private final JPasswordField regConfirm = new JPasswordField(18);
    private final JLabel registerMessage = new JLabel(" ");

    private final DefaultListModel<Concert> concertModel = new DefaultListModel<>();
    private final JList<Concert> concertList = new JList<>(concertModel);
    private final DefaultListModel<Ticket> ticketModel = new DefaultListModel<>();
    private final JList<Ticket> ticketList = new JList<>(ticketModel);
    private final DefaultListModel<Transaction> transactionModel = new DefaultListModel<>();
    private final JList<Transaction> transactionList = new JList<>(transactionModel);

    private final JTextField concertName = new JTextField(12), city = new JTextField(10), venue = new JTextField(12);
    private final JTextField startTime = new JTextField("2027-01-15T18:00", 16), endTime = new JTextField("2027-01-15T21:00", 16);
    private final JTextField premiumPrice = new JTextField("350000", 8), premiumQuota = new JTextField("100", 5);
    private final JTextField normalPrice = new JTextField("150000", 8), normalQuota = new JTextField("300", 5);
    private final JTextField quantity = new JTextField("1", 4), transactionId = new JTextField(6);
    private final JTextField staffUsername = new JTextField(14);
    private final JPasswordField staffPassword = new JPasswordField(14);
    private final JTextArea userArea = new JTextArea();
    private final JLabel status = new JLabel("Siap.");

    @FunctionalInterface public interface UserRepositoryAccess { List<String> allUsers() throws SQLException; }
    @FunctionalInterface public interface StaffCreator { void create(String username, String password) throws SQLException; }

    public MainFrame(AuthController authController, ConcertController concertController,
                     TransactionController transactionController, UserRepositoryAccess userAccess,
                     StaffCreator staffCreator) {
        this.authController = authController;
        this.concertController = concertController;
        this.transactionController = transactionController;
        this.userAccess = userAccess;
        this.staffCreator = staffCreator;

        setTitle("ConcertHub | Concert Ticket Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        pages.add(createLoginPage(), "LOGIN");
        pages.add(createRegisterPage(), "REGISTER");
        add(pages);

        new Timer(30_000, e -> {
            if (currentUser != null) {
                loadTransactions(); // history() sekaligus membatalkan PENDING yang lewat 15 menit
                loadTickets();
            }
        }).start();
    }

    private JPanel createLoginPage() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(WHITE);
        card.setBorder(new EmptyBorder(30, 38, 30, 38));
        card.setPreferredSize(new Dimension(440, 500));

        JPanel brand = new JPanel(new GridLayout(0, 1, 0, 5));
        brand.setBackground(WHITE);
        JLabel logo = new JLabel("CONCERTHUB", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 27));
        logo.setForeground(NAVY);
        JLabel tagline = new JLabel("YOUR NEXT CONCERT EXPERIENCE", SwingConstants.CENTER);
        tagline.setForeground(new Color(115, 127, 143));
        brand.add(logo); brand.add(tagline);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBackground(WHITE);
        addField(form, "Username", username);
        addField(form, "Password", password);
        JButton login = primaryButton("MASUK");
        login.addActionListener(e -> doLogin());
        form.add(login);
        loginMessage.setForeground(new Color(190, 55, 55));
        form.add(loginMessage);
        JLabel demo = new JLabel("Demo: admin/admin123  •  staff/staff123  •  customer/customer123", SwingConstants.CENTER);
        demo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        demo.setForeground(new Color(115, 127, 143));
        form.add(demo);

        JButton register = new JButton("Belum punya akun? Daftar sebagai Customer");
        styleLink(register);
        register.addActionListener(e -> { registerMessage.setText(" "); pageLayout.show(pages, "REGISTER"); });
        card.add(brand, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(register, BorderLayout.SOUTH);
        outer.add(card);
        password.addActionListener(e -> doLogin());
        return outer;
    }

    private JPanel createRegisterPage() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(WHITE);
        card.setBorder(new EmptyBorder(28, 38, 28, 38));
        card.setPreferredSize(new Dimension(440, 490));
        JLabel title = new JLabel("BUAT AKUN CUSTOMER", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(NAVY);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBackground(WHITE);
        addField(form, "Username (4-50 karakter)", regUsername);
        addField(form, "Password (minimal 6 karakter)", regPassword);
        addField(form, "Konfirmasi Password", regConfirm);
        JButton create = primaryButton("DAFTAR SEKARANG");
        create.addActionListener(e -> doRegister());
        form.add(create);
        registerMessage.setForeground(new Color(190, 55, 55));
        form.add(registerMessage);
        JButton back = new JButton("Kembali ke Login");
        styleLink(back);
        back.addActionListener(e -> pageLayout.show(pages, "LOGIN"));
        card.add(title, BorderLayout.NORTH); card.add(form, BorderLayout.CENTER); card.add(back, BorderLayout.SOUTH);
        outer.add(card);
        return outer;
    }

    private JPanel createDashboardPage() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel brand = new JLabel("CONCERTHUB  |  " + currentUser.getUsername() + "  •  " + currentUser.getRole());
        brand.setForeground(WHITE); brand.setFont(new Font("SansSerif", Font.BOLD, 19));
        header.add(brand, BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.setBackground(GOLD); logout.setForeground(NAVY); logout.setFocusPainted(false);
        logout.addActionListener(e -> logout());
        header.add(logout, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(WHITE);
        String role = currentUser.getRole();

        tabs.addTab("Konser", createConcertTab("ADMIN".equals(role)));
        if ("CUSTOMER".equals(role)) {
            tabs.addTab("Pesan Tiket", createBookingTab());
            tabs.addTab("Transaksi Saya", createTransactionsTab(false));
        } else {
            tabs.addTab("Transaksi & Verifikasi", createTransactionsTab(true));
            if ("ADMIN".equals(role)) tabs.addTab("Administrasi", createAdminTab());
        }
        root.add(tabs, BorderLayout.CENTER);
        status.setOpaque(true); status.setBackground(WHITE); status.setForeground(NAVY);
        status.setBorder(new EmptyBorder(8, 12, 8, 12));
        root.add(status, BorderLayout.SOUTH);
        return root;
    }

    private JPanel createConcertTab(boolean admin) {
        JPanel panel = basePanel(new BorderLayout(12, 12));
        JPanel heading = sectionCard("DAFTAR KONSER", "Pilih konser untuk melihat pilihan tiket dan ketersediaan.");
        concertList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        concertList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        concertList.setFixedCellHeight(76);
        concertList.setCellRenderer(new CardCellRenderer<>(c -> "#" + c.getId() + "  •  " + c.getName() + "\n" + c.getCity() + "  |  " + c.getVenue() + "\n" + c.getStartTime().toString().replace('T', ' ')));
        JScrollPane listScroll = new JScrollPane(concertList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());
        heading.add(listScroll, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setBackground(WHITE);
        JButton refresh = secondaryButton("Muat Ulang"); refresh.addActionListener(e -> { loadConcerts(); loadTickets(); });
        actions.add(refresh);
        if (admin) {
            JButton delete = dangerButton("Hapus Konser Terpilih");
            delete.addActionListener(e -> deleteSelectedConcert()); actions.add(delete);
        }
        heading.add(actions, BorderLayout.SOUTH);
        panel.add(heading, BorderLayout.CENTER);
        if (admin) panel.add(createConcertForm(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createConcertForm() {
        JPanel card = sectionCard("TAMBAH KONSER (ADMIN)", "Staff tidak memiliki akses untuk mengubah data konser.");
        JPanel fields = new JPanel(new GridLayout(0, 4, 8, 7)); fields.setBackground(WHITE);
        fields.add(new JLabel("Nama konser")); fields.add(concertName);
        fields.add(new JLabel("Kota")); fields.add(city);
        fields.add(new JLabel("Venue")); fields.add(venue);
        fields.add(new JLabel("Mulai (yyyy-MM-ddTHH:mm)")); fields.add(startTime);
        fields.add(new JLabel("Selesai (yyyy-MM-ddTHH:mm)")); fields.add(endTime);
        fields.add(new JLabel("Harga Premium")); fields.add(premiumPrice);
        fields.add(new JLabel("Kuota Premium")); fields.add(premiumQuota);
        fields.add(new JLabel("Harga Normal")); fields.add(normalPrice);
        fields.add(new JLabel("Kuota Normal")); fields.add(normalQuota);
        JButton save = primaryButton("Simpan Konser"); save.addActionListener(e -> addConcert());
        fields.add(save);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBookingTab() {
        JPanel panel = basePanel(new GridLayout(1, 2, 12, 12));
        JPanel tickets = sectionCard("PILIH TIKET", "Pilih kategori tiket yang ingin dibeli.");
        ticketList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ticketList.setFixedCellHeight(70);
        ticketList.setCellRenderer(new CardCellRenderer<>(t -> t.getConcertName() + "  •  " + t.getCategory() + "\nHarga: Rp" + t.getPrice().toPlainString() + "\nSisa tiket: " + t.getQuota()));
        tickets.add(new JScrollPane(ticketList), BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT)); controls.setBackground(WHITE);
        controls.add(new JLabel("Jumlah")); controls.add(quantity);
        JComboBox<String> method = new JComboBox<>(new String[]{"VA - BCA", "VA - BRI", "VA - BNI", "E-Wallet QR"});
        controls.add(method);
        JButton book = primaryButton("Pesan & Buat Pembayaran");
        book.addActionListener(e -> bookTicket(method.getSelectedItem().toString())); controls.add(book);
        tickets.add(controls, BorderLayout.SOUTH);
        JPanel info = sectionCard("INFORMASI PEMESANAN", "Alur pembelian tiket");
        JTextArea tips = new JTextArea("1. Pilih tiket Premium atau Normal.\n\n2. Tentukan jumlah tiket.\n\n3. Pilih metode pembayaran.\n\n4. Sistem membuat transaksi berstatus PENDING.\n\n5. Selesaikan pembayaran simulasi pada menu Transaksi Saya.\n\nKuota tiket otomatis berkurang saat pesanan dibuat.");
        tips.setEditable(false); tips.setLineWrap(true); tips.setWrapStyleWord(true); tips.setFont(new Font("SansSerif", Font.PLAIN, 14)); tips.setBackground(WHITE);
        info.add(tips, BorderLayout.CENTER);
        panel.add(tickets); panel.add(info);
        return panel;
    }

    private JPanel createTransactionsTab(boolean staffView) {
        JPanel panel = sectionCard(staffView ? "DAFTAR TRANSAKSI CUSTOMER" : "RIWAYAT TRANSAKSI SAYA",
                staffView ? "Staff memeriksa pembayaran; Admin dapat memantau seluruh transaksi." : "Riwayat ini hanya menampilkan transaksi akun yang sedang login. Tandai pesanan sebagai sudah dibayar setelah simulasi pembayaran.");
        transactionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && transactionList.getSelectedValue() != null) {
                transactionId.setText(String.valueOf(transactionList.getSelectedValue().getId()));
            }
        });
        transactionList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        transactionList.setFixedCellHeight(70);
        transactionList.setCellRenderer(new CardCellRenderer<>(t -> "Transaksi #" + t.getId() + "  •  " + t.getStatus() + "\n" + t.toString() + "\nTanggal: " + t.getCreatedAt().toString().replace('T', ' ')));
        panel.add(new JScrollPane(transactionList), BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT)); controls.setBackground(WHITE);
        JButton refresh = secondaryButton("Refresh Transaksi"); refresh.addActionListener(e -> loadTransactions()); controls.add(refresh);
        if (!staffView) {
            controls.add(new JLabel("ID transaksi")); controls.add(transactionId);
            JButton pay = primaryButton("Saya Sudah Bayar (Simulasi)");
            pay.addActionListener(e -> confirmPayment()); controls.add(pay);
        } else if ("STAFF".equals(currentUser.getRole())) {
            controls.add(new JLabel("ID transaksi")); controls.add(transactionId);
            JButton verify = primaryButton("Verifikasi & Selesaikan Pembayaran"); verify.addActionListener(e -> confirmPayment()); controls.add(verify);
        } else {
            controls.add(new JLabel("Admin dapat memantau transaksi. Verifikasi dilakukan Staff."));
        }
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdminTab() {
        JPanel panel = basePanel(new GridLayout(1, 2, 12, 12));
        JPanel users = sectionCard("MANAJEMEN AKUN", "Admin dapat melihat akun dan membuat akun Staff.");
        userArea.setEditable(false); userArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        users.add(new JScrollPane(userArea), BorderLayout.CENTER);
        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.LEFT)); userActions.setBackground(WHITE);
        JButton refresh = secondaryButton("Muat Akun"); refresh.addActionListener(e -> loadUsers()); userActions.add(refresh);
        users.add(userActions, BorderLayout.SOUTH);
        JPanel staff = sectionCard("TAMBAH STAFF", "Pendaftaran publik hanya dapat membuat akun Customer.");
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8)); form.setBackground(WHITE);
        addField(form, "Username Staff", staffUsername); addField(form, "Password Staff", staffPassword);
        JButton create = primaryButton("Buat Akun Staff"); create.addActionListener(e -> createStaff()); form.add(create);
        staff.add(form, BorderLayout.CENTER);
        panel.add(users); panel.add(staff);
        return panel;
    }

    private void doLogin() {
        try {
            currentUser = authController.login(username.getText(), new String(password.getPassword()));
            if (pages.getComponentCount() > 2) pages.remove(2);
            pages.add(createDashboardPage(), "DASHBOARD");
            pageLayout.show(pages, "DASHBOARD");
            loadConcerts(); loadTickets(); loadTransactions();
            if ("ADMIN".equals(currentUser.getRole())) loadUsers();
            showStatus("Login berhasil: " + currentUser.getUsername() + " • " + currentUser.getRole());
        } catch (SecurityException ex) { loginMessage.setText(ex.getMessage()); }
    }

    private void doRegister() {
        String p1 = new String(regPassword.getPassword());
        String p2 = new String(regConfirm.getPassword());
        if (!p1.equals(p2)) { registerMessage.setText("Konfirmasi password tidak sama."); return; }
        try {
            authController.registerCustomer(regUsername.getText(), p1);
            JOptionPane.showMessageDialog(this, "Akun Customer berhasil dibuat. Silakan login.", "Registrasi berhasil", JOptionPane.INFORMATION_MESSAGE);
            username.setText(regUsername.getText()); password.setText("");
            regUsername.setText(""); regPassword.setText(""); regConfirm.setText("");
            pageLayout.show(pages, "LOGIN");
        } catch (SecurityException ex) { registerMessage.setText(ex.getMessage()); }
    }

    private void logout() {
        currentUser = null; username.setText(""); password.setText(""); loginMessage.setText(" ");
        if (pages.getComponentCount() > 2) pages.remove(2);
        pageLayout.show(pages, "LOGIN");
    }

    private void loadConcerts() {
        try { concertModel.clear(); for (Concert c : concertController.concerts()) concertModel.addElement(c); }
        catch (Exception ex) { showStatus("Gagal memuat konser: " + ex.getMessage()); }
    }
    private void loadTickets() {
        try { ticketModel.clear(); for (Ticket t : concertController.tickets()) ticketModel.addElement(t); }
        catch (Exception ex) { showStatus("Gagal memuat tiket: " + ex.getMessage()); }
    }
    private void loadTransactions() {
        if (currentUser == null) return;
        try { transactionModel.clear(); for (Transaction t : transactionController.history(currentUser)) transactionModel.addElement(t); }
        catch (Exception ex) { showStatus("Gagal memuat transaksi: " + ex.getMessage()); }
    }
    private void loadUsers() {
        try { userArea.setText(String.join("\n", userAccess.allUsers())); }
        catch (SQLException ex) { showStatus("Gagal mengambil akun: " + ex.getMessage()); }
    }
    private void createStaff() {
        String u = staffUsername.getText().trim(), p = new String(staffPassword.getPassword());
        if (u.isBlank() || p.length() < 6) { showStatus("Username wajib diisi dan password minimal 6 karakter."); return; }
        try { staffCreator.create(u, p); staffUsername.setText(""); staffPassword.setText(""); loadUsers(); showStatus("Akun Staff berhasil dibuat."); }
        catch (SQLException ex) { showStatus("Gagal membuat Staff. Pastikan username belum digunakan."); }
    }
    private void addConcert() {
        try {
            concertController.addConcert(currentUser, concertName.getText(), city.getText(), venue.getText(),
                    LocalDateTime.parse(startTime.getText().trim()), LocalDateTime.parse(endTime.getText().trim()),
                    new BigDecimal(premiumPrice.getText().trim()), Integer.parseInt(premiumQuota.getText().trim()),
                    new BigDecimal(normalPrice.getText().trim()), Integer.parseInt(normalQuota.getText().trim()));
            showStatus("Konser berhasil ditambahkan."); loadConcerts(); loadTickets();
        } catch (DateTimeParseException ex) { showStatus("Format tanggal harus yyyy-MM-ddTHH:mm."); }
        catch (Exception ex) { showStatus("Gagal menambah konser: " + ex.getMessage()); }
    }
    private void deleteSelectedConcert() {
        Concert selected = concertList.getSelectedValue();
        if (selected == null) { showStatus("Pilih konser yang ingin dihapus."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus konser '" + selected.getName() + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try { concertController.deleteConcert(currentUser, selected.getId()); showStatus("Konser berhasil dihapus."); loadConcerts(); loadTickets(); }
        catch (Exception ex) { showStatus(ex.getMessage()); }
    }
    private void bookTicket(String method) {
        Ticket ticket = ticketList.getSelectedValue();
        if (ticket == null) { showStatus("Pilih tiket terlebih dahulu."); return; }
        try {
            int count = Integer.parseInt(quantity.getText().trim());
            PaymentStrategy strategy = method.startsWith("VA") ? new VAPayment(method.substring(5)) : new EWalletPayment();
            Transaction t = transactionController.book(currentUser, ticket, count, strategy);
            showStatus("Pesanan dibuat • ID #" + t.getId() + " • Total Rp" + t.getTotalPrice() + " • Status PENDING");
            loadTickets(); loadTransactions();
        } catch (NumberFormatException ex) { showStatus("Jumlah tiket harus berupa angka."); }
        catch (TicketUnavailableException ex) { showStatus(ex.getMessage()); }
        catch (Exception ex) { showStatus("Pemesanan gagal: " + ex.getMessage()); }
    }
    private void confirmPayment() {
        try {
            Transaction selected = transactionList.getSelectedValue();
            int id = selected != null ? selected.getId() : Integer.parseInt(transactionId.getText().trim());
            transactionController.confirm(currentUser, id);
            showStatus("Pembayaran berhasil diproses untuk transaksi #" + id + ".");
            transactionId.setText("");
            loadTransactions(); loadTickets();
        } catch (NumberFormatException ex) { showStatus("Masukkan ID transaksi yang benar."); }
        catch (Exception ex) { showStatus("Konfirmasi gagal: " + ex.getMessage()); }
    }

    private JPanel basePanel(LayoutManager layout) { JPanel p = new JPanel(layout); p.setBackground(BG); p.setBorder(new EmptyBorder(12, 4, 4, 4)); return p; }
    private JPanel sectionCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(8, 10)); card.setBackground(WHITE); card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel top = new JPanel(new GridLayout(0, 1, 0, 3)); top.setBackground(WHITE);
        JLabel h = new JLabel(title); h.setFont(new Font("SansSerif", Font.BOLD, 16)); h.setForeground(NAVY);
        JLabel sub = new JLabel(subtitle); sub.setFont(new Font("SansSerif", Font.PLAIN, 12)); sub.setForeground(new Color(110, 120, 135));
        top.add(h); top.add(sub); card.add(top, BorderLayout.NORTH); return card;
    }
    private void addField(JPanel panel, String label, JComponent field) {
        JLabel l = new JLabel(label); l.setForeground(NAVY); l.setFont(new Font("SansSerif", Font.BOLD, 12)); panel.add(l); panel.add(field);
    }
    private JButton primaryButton(String text) { JButton b = new JButton(text); b.setBackground(GOLD); b.setForeground(NAVY); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); return b; }
    private JButton secondaryButton(String text) { JButton b = new JButton(text); b.setBackground(BLUE); b.setForeground(WHITE); b.setFocusPainted(false); return b; }
    private JButton dangerButton(String text) { JButton b = new JButton(text); b.setBackground(new Color(174, 65, 65)); b.setForeground(WHITE); b.setFocusPainted(false); return b; }
    private void styleLink(JButton b) { b.setBorderPainted(false); b.setContentAreaFilled(false); b.setForeground(BLUE); b.setFocusPainted(false); }
    private void showStatus(String message) { status.setText(message); }

    private static class CardCellRenderer<T> implements ListCellRenderer<T> {
        private final Function<T, String> textProvider;
        CardCellRenderer(Function<T, String> textProvider) { this.textProvider = textProvider; }
        @Override public Component getListCellRendererComponent(JList<? extends T> list, T value, int index,
                                                                boolean selected, boolean focus) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? GOLD : new Color(225, 231, 239), 1),
                    new EmptyBorder(8, 12, 8, 12)));
            card.setBackground(selected ? new Color(255, 247, 229) : WHITE);
            JTextArea text = new JTextArea(textProvider.apply(value));
            text.setEditable(false); text.setFocusable(false); text.setOpaque(false);
            text.setFont(new Font("SansSerif", Font.PLAIN, 12));
            text.setForeground(NAVY); text.setLineWrap(true); text.setWrapStyleWord(true);
            card.add(text, BorderLayout.CENTER);
            return card;
        }
    }

}
