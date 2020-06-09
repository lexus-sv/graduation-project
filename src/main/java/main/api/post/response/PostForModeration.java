package main.api.post.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.user.UserBehavior;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostForModeration implements PostBehavior {
    private int id;
    private String time;
    private UserBehavior user;
    private String title;
    private String announce;
}
