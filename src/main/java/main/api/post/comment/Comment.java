
package main.api.post.comment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.user.UserFullInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    private int id;
    private String text;
    private String time;
    private UserFullInfo user;

}
