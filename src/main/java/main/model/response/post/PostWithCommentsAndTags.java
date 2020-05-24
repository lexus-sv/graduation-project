package main.model.response.post;

import main.model.response.Comment;
import main.model.response.user.UserBehavior;

import java.util.Date;
import java.util.List;

public class PostWithCommentsAndTags implements PostBehavior {
    private String text;

    private int commentCount;

    private int dislikeCount;

    private int id;

    private int likeCount;

    private Date time;

    private String title;

    private UserBehavior user;

    private int viewCount;
    private List<Comment> comments;
    private List<String> tags;

    public PostWithCommentsAndTags(int id, Date time, UserBehavior user, String title, String text, int commentCount, int dislikeCount,
                                   int likeCount,
                                   int viewCount, List<Comment> comments,
                                   List<String> tags) {
        this.text = text;
        this.commentCount = commentCount;
        this.dislikeCount = dislikeCount;
        this.id = id;
        this.likeCount = likeCount;
        this.time = time;
        this.title = title;
        this.user = user;
        this.viewCount = viewCount;
        this.comments = comments;
        this.tags = tags;
    }

    public PostWithCommentsAndTags() {
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public void setUser(UserBehavior user) {
        this.user = user;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
