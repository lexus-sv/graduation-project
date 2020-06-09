package main.api.post.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Posts implements PostBehavior {

    private int count;

    private List<PostBehavior> posts;

    public Posts(List<PostBehavior> posts) {
        this.count = posts.size();
        this.posts = posts;
    }
}
