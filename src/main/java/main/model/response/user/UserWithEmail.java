package main.model.response.user;

public class UserWithEmail implements UserBehavior {
    private int id;
    private String name;

    public UserWithEmail() {
    }

    public UserWithEmail(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
