import java.sql.*;
import java.util.*;

// All database operations in one place — simple and easy
public class DB {

    // ── LOGIN ─────────────────────────────────────────────────────────
    public static Person login(String username, String password) {
        String sql = "SELECT a.id, a.username, a.role, COALESCE(b.amount,0) as balance " +
                     "FROM accounts a LEFT JOIN balances b ON a.id = b.account_id " +
                     "WHERE a.username=? AND a.password=?";
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("role").equals("admin"))
                    return new Admin(rs.getInt("id"), rs.getString("username"));
                else
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getDouble("balance"));
            }
        } catch (Exception e) { System.out.println("Login error: " + e.getMessage()); }
        return null;
    }

    // ── GET BALANCE ───────────────────────────────────────────────────
    public static double getBalance(int accountId) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT amount FROM balances WHERE account_id=?")) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("amount");
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return 0;
    }

    // ── DEPOSIT ───────────────────────────────────────────────────────
    public static boolean deposit(int accountId, double amount) {
        if (amount <= 0) return false;
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            PreparedStatement p1 = c.prepareStatement("UPDATE balances SET amount = amount + ? WHERE account_id=?");
            p1.setDouble(1, amount); p1.setInt(2, accountId); p1.executeUpdate();

            PreparedStatement p2 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount,note) VALUES(?,?,?,?)");
            p2.setInt(1, accountId); p2.setString(2, "deposit");
            p2.setDouble(3, amount); p2.setString(4, "Deposit");
            p2.executeUpdate();
            c.commit();
            return true;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── WITHDRAW ──────────────────────────────────────────────────────
    public static boolean withdraw(int accountId, double amount) {
        if (amount <= 0 || getBalance(accountId) < amount) return false;
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            PreparedStatement p1 = c.prepareStatement("UPDATE balances SET amount = amount - ? WHERE account_id=?");
            p1.setDouble(1, amount); p1.setInt(2, accountId); p1.executeUpdate();

            PreparedStatement p2 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount,note) VALUES(?,?,?,?)");
            p2.setInt(1, accountId); p2.setString(2, "withdraw");
            p2.setDouble(3, amount); p2.setString(4, "Withdrawal");
            p2.executeUpdate();
            c.commit();
            return true;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── TRANSFER ──────────────────────────────────────────────────────
    public static String transfer(int fromId, int toId, double amount) {
        if (amount <= 0)                        return "Amount must be > 0";
        if (fromId == toId)                     return "Cannot transfer to yourself";
        if (getBalance(fromId) < amount)        return "Insufficient balance";
        if (getUserById(toId) == null)          return "Recipient not found";

        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            PreparedStatement p1 = c.prepareStatement("UPDATE balances SET amount = amount - ? WHERE account_id=?");
            p1.setDouble(1, amount); p1.setInt(2, fromId); p1.executeUpdate();

            PreparedStatement p2 = c.prepareStatement("UPDATE balances SET amount = amount + ? WHERE account_id=?");
            p2.setDouble(1, amount); p2.setInt(2, toId); p2.executeUpdate();

            PreparedStatement p3 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount,note) VALUES(?,?,?,?)");
            p3.setInt(1, fromId); p3.setString(2, "transfer");
            p3.setDouble(3, amount); p3.setString(4, "Sent to ID " + toId);
            p3.executeUpdate();

            PreparedStatement p4 = c.prepareStatement("INSERT INTO transactions(account_id,type,amount,note) VALUES(?,?,?,?)");
            p4.setInt(1, toId); p4.setString(2, "transfer");
            p4.setDouble(3, amount); p4.setString(4, "Received from ID " + fromId);
            p4.executeUpdate();

            c.commit();
            return "SUCCESS";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    // ── GET TRANSACTIONS ──────────────────────────────────────────────
    public static List<String[]> getTransactions(int accountId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT type, amount, note, done_at FROM transactions WHERE account_id=? ORDER BY done_at DESC";
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("type"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("note"),
                    rs.getString("done_at").substring(0, 16)
                });
            }
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return list;
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────
    public static boolean changePassword(int accountId, String newPass) {
        if (newPass.isEmpty()) return false;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE accounts SET password=? WHERE id=?")) {
            ps.setString(1, newPass); ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── SEND COMPLAINT ────────────────────────────────────────────────
    public static boolean sendComplaint(int accountId, String message) {
        if (message.isEmpty()) return false;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("INSERT INTO complaints(account_id,message) VALUES(?,?)")) {
            ps.setInt(1, accountId); ps.setString(2, message);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── ADMIN: GET ALL USERS ──────────────────────────────────────────
    public static List<String[]> getAllUsers() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.id, a.username, COALESCE(b.amount,0) as balance, a.role " +
                     "FROM accounts a LEFT JOIN balances b ON a.id = b.account_id";
        try (Connection c = Database.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("username"),
                    String.format("%.2f", rs.getDouble("balance")),
                    rs.getString("role")
                });
            }
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return list;
    }

    // ── ADMIN: CREATE USER ────────────────────────────────────────────
    public static boolean createUser(String username, String password, double balance) {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            PreparedStatement p1 = c.prepareStatement(
                "INSERT INTO accounts(username,password,role) VALUES(?,?,'user')",
                Statement.RETURN_GENERATED_KEYS);
            p1.setString(1, username); p1.setString(2, password);
            p1.executeUpdate();
            ResultSet keys = p1.getGeneratedKeys();
            keys.next();
            int newId = keys.getInt(1);

            PreparedStatement p2 = c.prepareStatement("INSERT INTO balances(account_id,amount) VALUES(?,?)");
            p2.setInt(1, newId); p2.setDouble(2, balance);
            p2.executeUpdate();
            c.commit();
            return true;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── ADMIN: DELETE USER ────────────────────────────────────────────
    public static boolean deleteUser(int accountId) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM accounts WHERE id=? AND role='user'")) {
            ps.setInt(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return false;
    }

    // ── ADMIN: GET COMPLAINTS ─────────────────────────────────────────
    public static List<String[]> getComplaints() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.username, c.message, c.sent_at FROM complaints c " +
                     "JOIN accounts a ON c.account_id = a.id ORDER BY c.sent_at DESC";
        try (Connection c = Database.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("username"),
                    rs.getString("message"),
                    rs.getString("sent_at").substring(0, 16)
                });
            }
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return list;
    }

    // ── HELPER ────────────────────────────────────────────────────────
    public static Person getUserById(int id) {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(
                "SELECT a.id, a.username, a.role, COALESCE(b.amount,0) as balance " +
                "FROM accounts a LEFT JOIN balances b ON a.id=b.account_id WHERE a.id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getString("role").equals("admin"))
                    return new Admin(rs.getInt("id"), rs.getString("username"));
                else
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getDouble("balance"));
            }
        } catch (Exception e) { System.out.println(e.getMessage()); }
        return null;
    }
}
