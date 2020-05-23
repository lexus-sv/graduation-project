
package main.model.response.post;

import main.model.response.user.User;
import main.model.response.user.UserBehavior;

import java.util.Date;
public class Post implements PostBehavior{

    private String announce;

    private int commentCount;

    private int dislikeCount;

    private int id;

    private int likeCount;

    private Date time;

    private String title;

    private UserBehavior user;

    private int viewCount;

    public Post(int id, Date time, UserBehavior user, String title, String announce, int likeCount, int dislikeCount, int commentCount,
                int viewCount) {
        this.announce = announce;
        this.commentCount = commentCount;
        this.dislikeCount = dislikeCount;
        this.id = id;
        this.likeCount = likeCount;
        this.time = time;
        this.title = title;
        this.user = user;
        this.viewCount = viewCount;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserBehavior getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

}
