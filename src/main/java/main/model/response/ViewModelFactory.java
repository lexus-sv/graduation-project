package main.model.response;

import main.model.PostVote;
import main.model.TagToPost;
import main.model.response.post.*;
import main.model.response.user.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ViewModelFactory {

    public static PostBehavior getPosts(List<main.model.Post> posts, PostModelType pt, UserModelType ut) {
        return createPosts(posts, pt, ut);
    }

    public static PostBehavior getSinglePost(main.model.Post post) {
        return getPostOfType(PostModelType.WITH_COMMENTS, post, getUserOfType(UserModelType.DEFAULT, post.getUser()));
    }

    public static UserBehavior getUserInfo(main.model.User user) {
        return getUserOfType(UserModelType.FULL_INFO, user);
    }

    /**
     * @param posts post from database
     * @param pt    needed post format for response
     * @param ut    needed user format for response
     */
    private static PostBehavior createPosts(List<main.model.Post> posts, PostModelType pt, UserModelType ut) {
        List<PostBehavior> formattedPosts = new ArrayList<>();
        posts.forEach(post -> {//For each post in the list formats the data for response
            UserBehavior user = getUserOfType(ut, post.getUser());
            PostBehavior p = getPostOfType(pt, post, user);
            formattedPosts.add(p);
        });
        return new Posts(formattedPosts);
    }

    /**
     * @param ut   user format for response
     * @param user user from database that needs to be formatted
     * @return formatted UserBehavior depending on user format.
     */
    private static UserBehavior getUserOfType(UserModelType ut, main.model.User user) {
        switch (ut) {
            case DEFAULT:
                return new User(user.getId(), user.getName());
            case FULL_INFO:
                return new UserFullInfo(user.getId(), user.getName(), user.getPhoto(), user.getEmail(),
                        user.isModerator(), user.getModeratedPosts().size(), user.isModerator());
            case WITH_PHOTO:
                return new UserWithPhoto(user.getId(), user.getName(), user.getPhoto());
            case WITH_EMAIL:
                return new UserWithEmail(user.getId(), user.getEmail());
            default:
                return null;
        }
    }

    /**
     * @param pt   post format for response
     * @param post post from database
     * @param user user-author of the post
     * @return PostBehavior with needed Post format
     */
    private static PostBehavior getPostOfType(PostModelType pt, main.model.Post post, UserBehavior user) {
        switch (pt) {
            case DEFAULT:
                return new Post(post.getId(), post.getTime(), user, post.getTitle(), post.getText(), (int) post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount());
            case WITH_COMMENTS:
                List<Comment> comments = new ArrayList<>();
                List<String> tags = new ArrayList<>();
                post.getPostComments().forEach(pc -> comments.add(new Comment(pc.getId(), pc.getText(), pc.getTime(), getUserOfType(UserModelType.WITH_PHOTO, post.getUser()))));
                post.getTags().forEach(tag -> tags.add(tag.getTag().getName()));
                return new PostWithCommentsAndTags(post.getId(), post.getTime(), user, post.getTitle(), post.getText(), (int) post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount(),
                        comments,
                        tags);
            case FOR_MODERATION:
                return new PostForModeration(post.getId(),post.getTime(), user, post.getTitle(), post.getText());
            default:
                return null;
        }
    }

    public static Tags getTags(List<main.model.Tag> tags) {
        List<Tag> formattedTags = new ArrayList<>();
        double size = 0;
        for (main.model.Tag tag : tags) {
            for (main.model.TagToPost ttp : tag.getTaggedPosts()) {
                if (ttp.getPost().isActive()) size++;
            }
        }
        double finalSize = size;
        double maxWeight = tags.stream().map(tag ->
                tag.getTaggedPosts().stream().filter(ttp-> ttp.getPost().isActive()).count() / finalSize).max(Double::compare).get();
        tags.forEach(tag ->formattedTags.add(new Tag(tag.getName(), ((double) tag.getTaggedPosts().stream().filter(ttp-> ttp.getPost().isActive()).count() / finalSize) / maxWeight)));
        return new Tags(formattedTags);
    }
}