
package main.api.response.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.response.user.User;
import main.api.response.user.UserBehavior;

import java.util.Date;
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
