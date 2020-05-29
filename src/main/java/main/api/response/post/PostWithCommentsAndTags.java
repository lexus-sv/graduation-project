package main.api.response.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.response.Comment;
import main.api.response.user.UserBehavior;

import java.util.Date;
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
