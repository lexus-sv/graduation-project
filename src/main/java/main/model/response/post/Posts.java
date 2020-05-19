package main.model.response.post;

import main.model.response.PostModelType;
import main.model.response.UserModelType;

import java.util.List;

public class Posts implements PostBehavior {

    private int count;

    private List<PostBehavior> posts;

    public Posts(List<PostBehavior> posts) {
        this.posts = posts;
        this.count = posts.size();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PostBehavior> getPosts() {
        return posts;
    }

    public void setPosts(List<PostBehavior> posts) {
        this.posts = posts;
    }
}
