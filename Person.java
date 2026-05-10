// Base class — inheritance used here
// Both User and Admin extend this
public class Person {

    int    id;
    String username;
    String role;

    public Person(int id, String username, String role) {
        this.id       = id;
        this.username = username;
        this.role     = role;
    }

    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getRole()     { return role; }
}
