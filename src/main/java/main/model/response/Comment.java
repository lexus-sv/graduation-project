
package main.model.response;


import main.model.response.user.UserBehavior;

import java.util.Date;

public class Comment {

    private int id;

    private String text;

    private Date time;

    private UserBehavior user;

    public Comment(int id, String text, Date time, UserBehavior user) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public UserBehavior getUser() {
        return user;
    }

    public void setUser(UserBehavior user) {
        this.user = user;
    }
}
