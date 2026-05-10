// User extends Person — inheritance
public class User extends Person {

    double balance;

    public User(int id, String username, double balance) {
        super(id, username, "user");
        this.balance = balance;
    }

    public double getBalance()            { return balance; }
    public void   setBalance(double b)    { this.balance = b; }
}
