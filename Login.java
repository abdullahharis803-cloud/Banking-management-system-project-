import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {

    JTextField     nameField;
    JPasswordField passField;
    JLabel         msgLabel;

    public Login() {
        setTitle("Bank System");
        setSize(360, 360);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(240, 238, 232));
        main.setBorder(new EmptyBorder(30, 40, 30, 40));
        add(main);

        JLabel title = new JLabel("Banking Management System");
        title.setFont(new Font("Georgia", Font.BOLD, 15));
        title.setForeground(new Color(42, 71, 55));
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);

        main.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Please login to continue");
        sub.setFont(new Font("Calibri", Font.PLAIN, 12));
        sub.setForeground(new Color(120, 115, 105));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        main.add(sub);

        main.add(Box.createVerticalStrut(25));

        nameField = makeField();
        passField = new JPasswordField();
        styleField(passField);

        addRow(main, "Username", nameField);
        addRow(main, "Password", passField);

        main.add(Box.createVerticalStrut(5));

        msgLabel = new JLabel(" ");
        msgLabel.setFont(new Font("Calibri", Font.PLAIN, 12));
        msgLabel.setForeground(new Color(180, 60, 60));
        msgLabel.setAlignmentX(LEFT_ALIGNMENT);
        main.add(msgLabel);

        main.add(Box.createVerticalStrut(10));

        JButton btn = makeBtn("Login", new Color(42, 71, 55));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        main.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String pass = new String(passField.getPassword()).trim();
                if (name.isEmpty() || pass.isEmpty()) {
                    msg("Fill all fields.", false); return;
                }
                Person p = DB.login(name, pass);
                if (p == null) {
                    msg("Wrong username or password.", false);
                } else {
                    dispose();
                    if (p.getRole().equals("admin")) new AdminPage((Admin) p);
                    else                              new UserPage((User) p);
                }
            }
        });

        setVisible(true);
    }

    void msg(String text, boolean ok) {
        msgLabel.setText(text);
        msgLabel.setForeground(ok ? new Color(42, 130, 75) : new Color(180, 60, 60));
    }

    void addRow(JPanel p, String label, JTextField field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Calibri", Font.BOLD, 13));
        l.setForeground(new Color(60, 60, 55));
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        p.add(Box.createVerticalStrut(12));
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
        b.setFont(new Font("Calibri", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { new Login(); }
        });
    }
}
