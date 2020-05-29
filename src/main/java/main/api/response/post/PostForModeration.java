package main.api.response.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.response.user.UserBehavior;

import java.util.Date;

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
