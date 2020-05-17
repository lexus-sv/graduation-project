package main.model.response;

import main.model.PostVote;
import main.model.response.post.Post;
import main.model.response.post.PostBehavior;
import main.model.response.post.PostWithCommentsAndTags;
import main.model.response.user.User;
import main.model.response.user.UserBehavior;
import main.model.response.user.UserFullInfo;
import main.model.response.user.UserWithPhoto;

import java.util.ArrayList;
import java.util.List;

public class PostGetModel {

    private int count;

    private List<PostBehavior> posts;

    public PostGetModel(int count, List<main.model.Post> posts, PostModelType pt, UserModelType ut) {
        this.count = count;
        this.posts = new ArrayList<>();
        posts.forEach(post -> {
            UserBehavior user = getUserOfType(ut, post.getUser());
            PostBehavior p = getPostOfType(pt, post, user);
            this.posts.add(p);
        });
    }

    private UserBehavior getUserOfType(UserModelType ut, main.model.User user){
        switch (ut){
            case DEFAULT: return new User(user.getId(), user.getName());
            case FULL_INFO: return new UserFullInfo(user.getId(), user.getName(), user.getPhoto(), user.getEmail(),
                        user.isModerator(), user.getModeratedPosts().size(), user.isModerator());
            case WITH_PHOTO: return new UserWithPhoto(user.getId(), user.getName(), user.getPhoto());
            default: return null;
        }
    }

    private PostBehavior getPostOfType(PostModelType pt, main.model.Post post, UserBehavior user){
        switch (pt){
            case DEFAULT:
                return new Post(post.getId(), post.getTime(), user, post.getTitle(), post.getText(), (int)post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int)post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount());
            case WITH_COMMENTS:
                List<Comment> comments = new ArrayList<>();
                List<String> tags = new ArrayList<>();
                post.getPostComments().forEach(pc->comments.add(new Comment(pc.getId(), pc.getText(), pc.getTime(), getUserOfType(UserModelType.WITH_PHOTO, post.getUser()))));
                post.getTags().forEach(tag->tags.add(tag.getTag().getName()));
                return new PostWithCommentsAndTags(post.getId(), post.getTime(), user, post.getTitle(), post.getText(), (int)post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int)post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount(),
                        comments,
                        tags);
            default: return null;
        }
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
