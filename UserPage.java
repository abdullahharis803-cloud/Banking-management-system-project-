import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class UserPage extends JFrame {

    User   user;
    JLabel balLabel;
    JLabel msgLabel;

    public UserPage(User user) {
        this.user = user;

        setTitle("Bank System - " + user.getUsername());
        setSize(480, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(240, 238, 232));
        main.setBorder(new EmptyBorder(20, 35, 20, 35));
        add(main);

        // ── Title ──────────────────────────────────────────────────────
        JLabel title = new JLabel("Banking Management System");
        title.setFont(new Font("Georgia", Font.BOLD, 15));
        title.setForeground(new Color(42, 71, 55));
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);

        main.add(Box.createVerticalStrut(4));

        JLabel welcome = new JLabel("Welcome, " + user.getUsername());
        welcome.setFont(new Font("Calibri", Font.PLAIN, 12));
        welcome.setForeground(new Color(120, 115, 105));
        welcome.setAlignmentX(CENTER_ALIGNMENT);
        main.add(welcome);

        main.add(Box.createVerticalStrut(15));

        // ── Balance Box ────────────────────────────────────────────────
        JPanel balBox = new JPanel();
        balBox.setLayout(new BoxLayout(balBox, BoxLayout.Y_AXIS));
        balBox.setBackground(new Color(42, 71, 55));
        balBox.setBorder(new EmptyBorder(12, 20, 12, 20));
        balBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        balBox.setAlignmentX(LEFT_ALIGNMENT);

        JLabel balTitle = new JLabel("Current Balance");
        balTitle.setFont(new Font("Calibri", Font.PLAIN, 11));
        balTitle.setForeground(new Color(144, 190, 155));
        balTitle.setAlignmentX(CENTER_ALIGNMENT);

        balLabel = new JLabel("$ " + String.format("%.2f", user.getBalance()));
        balLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        balLabel.setForeground(new Color(233, 196, 106));
        balLabel.setAlignmentX(CENTER_ALIGNMENT);

        balBox.add(balTitle);
        balBox.add(balLabel);
        main.add(balBox);

        main.add(Box.createVerticalStrut(18));

        // ── Tabs ───────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Calibri", Font.BOLD, 12));
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addTab("Deposit",    buildMoneyTab("deposit"));
        tabs.addTab("Withdraw",   buildMoneyTab("withdraw"));
        tabs.addTab("Transfer",   buildTransferTab());
        tabs.addTab("History",    buildHistoryTab());
        tabs.addTab("Password",   buildPasswordTab());
        tabs.addTab("Complaint",  buildComplaintTab());
        main.add(tabs);

        main.add(Box.createVerticalStrut(10));

        // ── Message ────────────────────────────────────────────────────
        msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
        msgLabel.setAlignmentX(LEFT_ALIGNMENT);
        main.add(msgLabel);

        // ── Logout ─────────────────────────────────────────────────────
        JButton logout = makeBtn("Logout", new Color(120, 60, 60));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); new Login();
            }
        });
        main.add(Box.createVerticalStrut(6));
        main.add(logout);

        setVisible(true);
    }

    // ── Deposit / Withdraw Tab ─────────────────────────────────────────
    JPanel buildMoneyTab(String type) {
        JPanel p = makeTabPanel();
        JTextField amtField = makeField();
        addRow(p, "Amount ($)", amtField);
        p.add(Box.createVerticalStrut(8));

        String btnColor = type.equals("deposit") ? "#2A4737" : "#7B3A1E";
        JButton btn = makeBtn(type.substring(0,1).toUpperCase()+type.substring(1),
                              type.equals("deposit") ? new Color(42,71,55) : new Color(123,58,30));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amtField.getText().trim());
                    boolean ok = type.equals("deposit") ? DB.deposit(user.getId(), amount)
                                                        : DB.withdraw(user.getId(), amount);
                    if (ok) {
                        user.setBalance(DB.getBalance(user.getId()));
                        balLabel.setText("$ " + String.format("%.2f", user.getBalance()));
                        amtField.setText("");
                        msg(type + " successful.", true);
                    } else {
                        msg("Failed. Check amount or balance.", false);
                    }
                } catch (NumberFormatException ex) {
                    msg("Enter a valid number.", false);
                }
            }
        });
        return p;
    }

    // ── Transfer Tab ───────────────────────────────────────────────────
    JPanel buildTransferTab() {
        JPanel p = makeTabPanel();
        JTextField toField  = makeField();
        JTextField amtField = makeField();
        addRow(p, "Recipient Account ID", toField);
        addRow(p, "Amount ($)", amtField);
        p.add(Box.createVerticalStrut(5));

        JButton btn = makeBtn("Transfer", new Color(43, 76, 120));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int    toId   = Integer.parseInt(toField.getText().trim());
                    double amount = Double.parseDouble(amtField.getText().trim());
                    String result = DB.transfer(user.getId(), toId, amount);
                    if (result.equals("SUCCESS")) {
                        user.setBalance(DB.getBalance(user.getId()));
                        balLabel.setText("$ " + String.format("%.2f", user.getBalance()));
                        toField.setText(""); amtField.setText("");
                        msg("Transfer successful.", true);
                    } else {
                        msg(result, false);
                    }
                } catch (NumberFormatException ex) {
                    msg("Enter valid numbers.", false);
                }
            }
        });
        return p;
    }

    // ── History Tab ────────────────────────────────────────────────────
    JPanel buildHistoryTab() {
        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.setBackground(new Color(240, 238, 232));
        p.setBorder(new EmptyBorder(12, 10, 12, 10));

        String[] cols = {"Type", "Amount", "Note", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Calibri", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(42, 71, 55));
        table.getTableHeader().setForeground(Color.WHITE);

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = makeBtn("Refresh", new Color(42, 71, 55));
        p.add(refresh, BorderLayout.SOUTH);

        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.setRowCount(0);
                List<String[]> rows = DB.getTransactions(user.getId());
                for (String[] row : rows) model.addRow(row);
            }
        });

        // load on open
        List<String[]> rows = DB.getTransactions(user.getId());
        for (String[] row : rows) model.addRow(row);

        return p;
    }

    // ── Password Tab ───────────────────────────────────────────────────
    JPanel buildPasswordTab() {
        JPanel p = makeTabPanel();
        JPasswordField newPass = new JPasswordField();
        styleField(newPass);
        newPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        newPass.setAlignmentX(LEFT_ALIGNMENT);
        addRow(p, "New Password", newPass);
        p.add(Box.createVerticalStrut(5));

        JButton btn = makeBtn("Change Password", new Color(42, 71, 55));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String pass = new String(newPass.getPassword()).trim();
                if (DB.changePassword(user.getId(), pass)) {
                    newPass.setText("");
                    msg("Password changed.", true);
                } else {
                    msg("Failed. Password cannot be empty.", false);
                }
            }
        });
        return p;
    }

    // ── Complaint Tab ──────────────────────────────────────────────────
    JPanel buildComplaintTab() {
        JPanel p = makeTabPanel();

        JTextArea area = new JTextArea(4, 10);
        area.setFont(new Font("Calibri", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 185, 175)),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        p.add(new JLabel("Write your complaint:") {{ setFont(new Font("Calibri", Font.BOLD, 12)); setForeground(new Color(60,60,55)); setAlignmentX(LEFT_ALIGNMENT); }});
        p.add(Box.createVerticalStrut(5));
        p.add(scroll);
        p.add(Box.createVerticalStrut(8));

        JButton btn = makeBtn("Send to Admin", new Color(120, 60, 60));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = area.getText().trim();
                if (DB.sendComplaint(user.getId(), text)) {
                    area.setText("");
                    msg("Complaint sent to admin.", true);
                } else {
                    msg("Write something first.", false);
                }
            }
        });
        return p;
    }

    // ── Helpers ────────────────────────────────────────────────────────
    void msg(String text, boolean ok) {
        msgLabel.setText(text);
        msgLabel.setForeground(ok ? new Color(42,130,75) : new Color(180,60,60));
    }

    JPanel makeTabPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(240, 238, 232));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        return p;
    }

    void addRow(JPanel p, String label, JTextField field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Calibri", Font.BOLD, 12));
        l.setForeground(new Color(60, 60, 55));
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        p.add(Box.createVerticalStrut(10));
    }

    JTextField makeField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    void styleField(JTextField f) {
        f.setFont(new Font("Calibri", Font.PLAIN, 13));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 185, 175), 1),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Calibri", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
