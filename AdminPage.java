import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AdminPage extends JFrame {

    Admin  admin;
    JLabel msgLabel;

    public AdminPage(Admin admin) {
        this.admin = admin;

        setTitle("Bank System - Admin");
        setSize(600, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(240, 238, 232));
        main.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(main);

        JLabel title = new JLabel("Banking Management System - Admin");
        title.setFont(new Font("Georgia", Font.BOLD, 15));
        title.setForeground(new Color(42, 71, 55));
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);

        main.add(Box.createVerticalStrut(15));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Calibri", Font.BOLD, 12));
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        tabs.addTab("All Accounts",   buildAccountsTab());
        tabs.addTab("Create Account", buildCreateTab());
        tabs.addTab("Complaints",     buildComplaintsTab());
        main.add(tabs);

        main.add(Box.createVerticalStrut(10));

        msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
        msgLabel.setAlignmentX(LEFT_ALIGNMENT);
        main.add(msgLabel);

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

    // ── All Accounts Tab ───────────────────────────────────────────────
    JPanel buildAccountsTab() {
        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.setBackground(new Color(240, 238, 232));
        p.setBorder(new EmptyBorder(12, 10, 12, 10));

        String[] cols = {"ID", "Username", "Balance", "Role"};
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

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottom.setBackground(new Color(240, 238, 232));

        JButton refresh = makeBtn("Refresh", new Color(42, 71, 55));
        JButton delete  = makeBtn("Delete Selected", new Color(120, 60, 60));
        bottom.add(refresh);
        bottom.add(delete);
        p.add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.setRowCount(0);
                for (String[] row : DB.getAllUsers()) model.addRow(row);
            }
        });

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) { msg("Select a row first.", false); return; }
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                int confirm = JOptionPane.showConfirmDialog(null, "Delete account #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (DB.deleteUser(id)) {
                        model.removeRow(row);
                        msg("Account deleted.", true);
                    } else {
                        msg("Cannot delete admin.", false);
                    }
                }
            }
        });

        // load on open
        for (String[] row : DB.getAllUsers()) model.addRow(row);
        return p;
    }

    // ── Create Account Tab ─────────────────────────────────────────────
    JPanel buildCreateTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(240, 238, 232));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField nameField  = makeField();
        JTextField passField  = makeField();
        JTextField balField   = makeField();

        addRow(p, "Username",        nameField);
        addRow(p, "Password",        passField);
        addRow(p, "Initial Balance", balField);

        p.add(Box.createVerticalStrut(5));

        JButton btn = makeBtn("Create Account", new Color(42, 71, 55));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        p.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String pass = passField.getText().trim();
                String bal  = balField.getText().trim();
                if (name.isEmpty() || pass.isEmpty() || bal.isEmpty()) {
                    msg("Fill all fields.", false); return;
                }
                try {
                    double balance = Double.parseDouble(bal);
                    if (DB.createUser(name, pass, balance)) {
                        nameField.setText(""); passField.setText(""); balField.setText("");
                        msg("Account created for " + name, true);
                    } else {
                        msg("Username already exists.", false);
                    }
                } catch (NumberFormatException ex) {
                    msg("Balance must be a number.", false);
                }
            }
        });
        return p;
    }

    // ── Complaints Tab ─────────────────────────────────────────────────
    JPanel buildComplaintsTab() {
        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.setBackground(new Color(240, 238, 232));
        p.setBorder(new EmptyBorder(12, 10, 12, 10));

        String[] cols = {"Username", "Complaint", "Date"};
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
                for (String[] row : DB.getComplaints()) model.addRow(row);
            }
        });

        for (String[] row : DB.getComplaints()) model.addRow(row);
        return p;
    }

    // ── Helpers ────────────────────────────────────────────────────────
    void msg(String text, boolean ok) {
        msgLabel.setText(text);
        msgLabel.setForeground(ok ? new Color(42,130,75) : new Color(180,60,60));
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
