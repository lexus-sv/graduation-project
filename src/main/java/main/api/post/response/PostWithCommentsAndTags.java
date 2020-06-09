package main.api.post.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.post.comment.Comment;
import main.api.user.UserBehavior;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostWithCommentsAndTags implements PostBehavior {
    private int id;
    private String time;
    private UserBehavior user;
    private String title;
    private String text;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
    private List<Comment> comments;
    private List<String> tags;
}
