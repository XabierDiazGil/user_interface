package es.deusto.sd.user_interface.entity;

public class UserItem {
    private static int ID_GEN = 1;

    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final String phone;

    public UserItem(String name, String email, String password, String phone) {
        this.id = ID_GEN++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return id + ": " + name + " <" + email + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserItem)) return false;
        UserItem u = (UserItem) o;
        return email != null && email.equals(u.email);
    }

    @Override
    public int hashCode() {
        return email == null ? 0 : email.hashCode();
    }
}