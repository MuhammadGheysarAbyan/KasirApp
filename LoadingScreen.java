import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingScreen extends JFrame {
    private JProgressBar progressBar;
    private Timer timer;
    private int progress = 0;
    private JLabel lblLoading;
    private JLabel lblPercent;
    
    // Colors matching login form
    private static final Color BLUE_PRIMARY = new Color(0, 102, 204);
    private static final Color TEXT_DARK = new Color(44, 62, 80);
    private static final Color TEXT_GRAY = new Color(80, 80, 80);

    public LoadingScreen() {
        setTitle("Kasir App - Loading...");
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Gradient background sama dengan login
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                Color color1 = BLUE_PRIMARY;
                Color color2 = Color.WHITE;
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(new GridBagLayout());
        add(background);
        
        // Card container
        JPanel card = createCard();
        background.add(card);
        
        startLoading();
        setVisible(true);
    }
    
    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(450, 400));
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Logo
        JLabel lblLogo = createLogo();
        card.add(lblLogo, gbc);
        
        // Title
        gbc.gridy++;
        JLabel lblTitle = new JLabel("KASIR APP", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(TEXT_DARK);
        card.add(lblTitle, gbc);
        
        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(5, 20, 20, 20);
        JLabel lblSubtitle = new JLabel("Bynest", JLabel.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_GRAY);
        card.add(lblSubtitle, gbc);
        
        // Loading status
        gbc.gridy++;
        gbc.insets = new Insets(20, 20, 10, 20);
        lblLoading = new JLabel("Initializing application...", JLabel.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblLoading.setForeground(TEXT_GRAY);
        card.add(lblLoading, gbc);
        
        // Progress bar
        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressBar = createProgressBar();
        card.add(progressBar, gbc);
        
        // Percentage
        gbc.gridy++;
        gbc.insets = new Insets(5, 20, 10, 20);
        gbc.fill = GridBagConstraints.NONE;
        lblPercent = new JLabel("0%", JLabel.CENTER);
        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPercent.setForeground(BLUE_PRIMARY);
        card.add(lblPercent, gbc);
        
        // Loading dots
        gbc.gridy++;
        gbc.insets = new Insets(10, 20, 20, 20);
        JLabel lblDots = createLoadingDots();
        card.add(lblDots, gbc);
        
        return card;
    }
    
    private JLabel createLogo() {
        JLabel lblLogo;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/img/logo_bg.png"));
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblLogo = new JLabel(new ImageIcon(img), JLabel.CENTER);
        } catch (Exception e) {
            // Custom drawn logo
            lblLogo = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int size = 60;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    
                    // Modern cart icon
                    g2.setColor(BLUE_PRIMARY);
                    g2.fillRoundRect(x, y, size, size, 15, 15);
                    
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(3f));
                    
                    // Cart body
                    g2.drawRect(x + 15, y + 20, 30, 20);
                    // Handle
                    g2.drawLine(x + 10, y + 20, x + 15, y + 20);
                    g2.drawLine(x + 10, y + 20, x + 10, y + 30);
                    // Wheels
                    g2.fillOval(x + 18, y + 42, 6, 6);
                    g2.fillOval(x + 36, y + 42, 6, 6);
                }
            };
            lblLogo.setPreferredSize(new Dimension(80, 80));
        }
        return lblLogo;
    }
    
    private JProgressBar createProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Background
                g2.setColor(new Color(230, 230, 230));
                g2.fillRoundRect(0, 0, w, h, h, h);
                
                // Progress
                if (getValue() > 0) {
                    int progressWidth = (int) ((double) getValue() / getMaximum() * w);
                    g2.setColor(BLUE_PRIMARY);
                    g2.fillRoundRect(0, 0, progressWidth, h, h, h);
                    
                    // Shine effect
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillRoundRect(0, 0, progressWidth, h/2, h/2, h/2);
                }
            }
        };
        bar.setPreferredSize(new Dimension(300, 12));
        bar.setOpaque(false);
        bar.setBorderPainted(false);
        return bar;
    }
    
    private JLabel createLoadingDots() {
        JLabel lblDots = new JLabel("●●●", JLabel.CENTER);
        lblDots.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDots.setForeground(BLUE_PRIMARY);
        
        // Animate dots
        Timer dotsTimer = new Timer(400, new ActionListener() {
            private int dotState = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] states = {"●○○", "●●○", "●●●", "○●●", "○○●", "○○○"};
                lblDots.setText(states[dotState]);
                dotState = (dotState + 1) % states.length;
            }
        });
        dotsTimer.start();
        
        return lblDots;
    }
    
    private void startLoading() {
        timer = new Timer(35, e -> {
            progress++;
            progressBar.setValue(progress);
            lblPercent.setText(progress + "%");
            
            // Update status
            if (progress < 20) {
                lblLoading.setText("Loading core modules...");
            } else if (progress < 40) {
                lblLoading.setText("Connecting to database...");
            } else if (progress < 60) {
                lblLoading.setText("Loading user interface...");
            } else if (progress < 80) {
                lblLoading.setText("Initializing components...");
            } else if (progress < 95) {
                lblLoading.setText("Preparing system...");
            } else {
                lblLoading.setText("Almost ready...");
            }
            
            if (progress >= 100) {
                timer.stop();
                lblLoading.setText("Ready!");
                
                Timer delayTimer = new Timer(500, evt -> {
                    ((Timer) evt.getSource()).stop();
                    openLogin();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
        timer.start();
    }
    
    private void openLogin() {
        // Simple fade effect
        Timer fadeTimer = new Timer(30, new ActionListener() {
            float opacity = 1.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0) {
                    ((Timer) e.getSource()).stop();
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        new LoginForm().setVisible(true);
                    });
                } else {
                    setOpacity(Math.max(0, opacity));
                }
            }
        });
        fadeTimer.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoadingScreen();
        });
    }
}