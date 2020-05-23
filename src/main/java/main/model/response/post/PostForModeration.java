package main.model.response.post;

import main.model.response.user.UserBehavior;

import java.util.Date;

public class PostForModeration implements PostBehavior {
    private int id;
    private Date time;
    private UserBehavior user;
    private String title;
    private String announce;

    public PostForModeration() {
    }

    public PostForModeration(int id, Date time, UserBehavior user, String title, String announce) {
        this.id = id;
        this.time = time;
        this.user = user;
        this.title = title;
        this.announce = announce;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }
}
