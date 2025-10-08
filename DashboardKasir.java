import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.image.BufferedImage;

public class DashboardKasir extends JFrame {

    private String kasir;
    private JLabel lblTime;
    private Timer clockTimer;
    private Connection conn;

    // Shift
    private String shift = "Tidak diketahui";

    // Colors
    private static final Color BLUE_PRIMARY = new Color(0, 102, 204);
    private static final Color TEXT_DARK = new Color(44, 62, 80);

    // Formatter untuk rupiah
    private static final DecimalFormat df = new DecimalFormat("#,###");

    // ✅ Tambahan constructor agar bisa dipanggil dari LoginForm (username, kodeUser, shift)
    public DashboardKasir(String kasir, String kodeUser, String shift) {
        this(kasir); // panggil constructor utama
        this.shift = shift; // set shift dari LoginForm
    }

    public DashboardKasir(String kasir) {
        this.kasir = kasir;
        setTitle("🏠 Dashboard Kasir - Kasir App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        connectDatabase();
        loadShiftFromDB(); // ambil shift dari DB

        // Background panel
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BLUE_PRIMARY, 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(255, 255, 255, 6));
                for (int x = 0; x < getWidth(); x += 50) {
                    for (int y = 0; y < getHeight(); y += 50) {
                        g2.fillOval(x, y, 2, 2);
                    }
                }
            }
        };
        background.setLayout(new BorderLayout());
        add(background, BorderLayout.CENTER);

        background.add(createTopPanel(), BorderLayout.NORTH);
        background.add(createCenterPanel(), BorderLayout.CENTER);

        startClock();
    }

    private void connectDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/kasir_db";
            String user = "root";
            String password = "";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected ✅");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Koneksi database gagal!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadShiftFromDB() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT shift FROM users WHERE username = ?");
            ps.setString(1, kasir);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) shift = rs.getString("shift");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);
    topPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));

    // Panel kiri (Logo + Welcome)
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
    leftPanel.setOpaque(false);

    JLabel lblLogo = new JLabel(loadRoundedLogo("/img/logo.jpg"));
    // Margin: top 15px, kanan 20px
    lblLogo.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 20));
    leftPanel.add(lblLogo);

    JPanel welcomePanel = new JPanel();
    welcomePanel.setOpaque(false);
    welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

    JLabel lblWelcome = new JLabel("Selamat datang, " + kasir);
    lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
    lblWelcome.setForeground(Color.WHITE);

    JLabel lblRole = new JLabel("Kasir Dashboard");
    lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblRole.setForeground(new Color(255, 255, 255, 180));

    JLabel lblShift = new JLabel("Shift: " + shift);
    lblShift.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblShift.setForeground(new Color(255, 255, 255, 200));

    welcomePanel.add(lblWelcome);
    welcomePanel.add(lblRole);
    welcomePanel.add(lblShift);

    leftPanel.add(welcomePanel);

    // Panel kanan (Jam)
    lblTime = new JLabel();
    lblTime.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblTime.setForeground(Color.WHITE);
    lblTime.setHorizontalAlignment(SwingConstants.RIGHT);

    topPanel.add(leftPanel, BorderLayout.WEST);
    topPanel.add(lblTime, BorderLayout.EAST);

    return topPanel;
}


    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));

        centerPanel.add(createMenuCard(), BorderLayout.CENTER);
        centerPanel.add(createInfoPanel(), BorderLayout.SOUTH);

        return centerPanel;
    }

    private JPanel createMenuCard() {
        JPanel card = new JPanel(new GridLayout(1, 3, 25, 25));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JButton btnTransaksi = createMenuButton("Transaksi Penjualan", new Color(52, 152, 219), "/img/chart.png");
        JButton btnRiwayat = createMenuButton("Riwayat Transaksi", new Color(46, 204, 113), "/img/riwayat.png");
        JButton btnLogout = createMenuButton("Logout", new Color(231, 76, 60), "/img/logout.png");

        btnTransaksi.addActionListener(e -> {
            new TransaksiPenjualan(kasir).setVisible(true);
            dispose();
        });
        btnRiwayat.addActionListener(e -> {
            new RiwayatTransaksi(kasir).setVisible(true);
            dispose();
        });
        btnLogout.addActionListener(e -> {
            if (clockTimer != null) clockTimer.stop();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        card.add(btnTransaksi);
        card.add(btnRiwayat);
        card.add(btnLogout);

        return card;
    }

    private JButton createMenuButton(String text, Color bgColor, String imgPath) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(160, 130));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(bgColor);
        content.setOpaque(true);
        content.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        ImageIcon icon = loadImageIcon(imgPath);
        JLabel lblIcon = new JLabel(icon, JLabel.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        JLabel lblText = new JLabel(text, JLabel.CENTER);
        lblText.setForeground(Color.WHITE);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblSubtitle = new JLabel("", JLabel.CENTER);
        lblSubtitle.setForeground(new Color(255, 255, 255, 180));
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        textPanel.add(lblText);
        textPanel.add(lblSubtitle);

        content.add(lblIcon, BorderLayout.CENTER);
        content.add(textPanel, BorderLayout.SOUTH);

        btn.add(content, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { content.setBackground(bgColor.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { content.setBackground(bgColor); }
        });

        return btn;
    }

    private ImageIcon loadImageIcon(String path) {
        try {
            if (path.startsWith("/")) path = path.substring(1);
            java.net.URL imgURL = getClass().getClassLoader().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else return createPlaceholderIcon();
        } catch (Exception e) { return createPlaceholderIcon(); }
    }

private ImageIcon loadRoundedLogo(String path) {
    try {
        // Pakai resource supaya aman di NetBeans & build JAR
        BufferedImage img = javax.imageio.ImageIO.read(getClass().getResourceAsStream(path));
        int size = 80; // ukuran logo
        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);

        BufferedImage rounded = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rounded.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Lengkungan lebih landai (arc 10)
        g2.setClip(new java.awt.geom.RoundRectangle2D.Double(0, 0, size, size, 10, 10));
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();

        return new ImageIcon(rounded);
    } catch (Exception e) {
        e.printStackTrace();
        return createPlaceholderIcon();
    }
}


    private ImageIcon createPlaceholderIcon() {
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 40, 40);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(0, 0, 39, 39);
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawLine(0, 0, 39, 39);
        g2d.drawLine(39, 0, 0, 39);
        g2d.dispose();
        return new ImageIcon(image);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 25, 0));
        infoPanel.setOpaque(false);
        infoPanel.setPreferredSize(new Dimension(0, 70));

        infoPanel.add(createInfoCard("Total Produk", getDBCount("produk", "id"), new Color(52, 152, 219)));
        infoPanel.add(createInfoCard("Total User", getDBCount("users", "id"), new Color(46, 204, 113)));
        infoPanel.add(createInfoCard("Transaksi Hari Ini", getDBCountToday("transaksi", "tanggal"), new Color(241, 196, 15)));
        infoPanel.add(createInfoCard("Pendapatan Hari Ini", getDBSumToday("transaksi", "total"), new Color(155, 89, 182)));

        return infoPanel;
    }

    private JPanel createInfoCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(TEXT_DARK);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(120, 120, 120));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblTitle);

        card.add(textPanel, BorderLayout.CENTER);

        JPanel accentWrapper = new JPanel(new BorderLayout());
        accentWrapper.setOpaque(false);
        accentWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(4, 0));
        accentWrapper.add(accent, BorderLayout.CENTER);

        card.add(accentWrapper, BorderLayout.WEST);

        return card;
    }

    private String getDBCount(String table, String column) {
        String result = "0";
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(" + column + ") FROM " + table);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) result = rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private String getDBCountToday(String table, String dateColumn) {
        String result = "0";
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM " + table + " WHERE DATE(" + dateColumn + ") = CURDATE()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) result = rs.getString(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private String getDBSumToday(String table, String column) {
        String result = "0";
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT SUM(" + column + ") FROM " + table + " WHERE DATE(tanggal) = CURDATE()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) result = rs.getString(1);
            if (result == null) result = "0";
            double total = Double.parseDouble(result);
            result = "Rp " + df.format(total);
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private void startClock() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String dateStr = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        lblTime.setText("<html><div style='text-align: right'>" + timeStr + "<br>" + dateStr + "</div></html>");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardKasir("admin").setVisible(true));
    }
}
