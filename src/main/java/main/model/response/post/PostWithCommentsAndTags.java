package main.model.response.post;

import main.model.response.Comment;
import main.model.response.post.Post;
import main.model.response.user.UserBehavior;

import java.util.Date;
import java.util.List;

public class PostWithCommentsAndTags extends Post {
    private List<Comment> comments;
    private List<String> tags;

    public PostWithCommentsAndTags(int id, Date time, UserBehavior user, String title, String announce, int likeCount, int dislikeCount, int commentCount, int viewCount, List<Comment> comments, List<String> tags) {
        super(id, time, user, title, announce, likeCount, dislikeCount, commentCount, viewCount);
        this.comments = comments;
        this.tags = tags;
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
}
