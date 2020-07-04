package main.api.post.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Posts {

  private List<Post> posts;

  private int count;

}
