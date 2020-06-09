
package main.api.post.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.user.UserBehavior;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post implements PostBehavior{

    private int id;
    private String time;
    private UserBehavior user;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;

}
