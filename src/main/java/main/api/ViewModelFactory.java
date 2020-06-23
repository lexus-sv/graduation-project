package main.api;

import main.api.post.PostModelType;
import main.api.post.comment.Comment;
import main.api.post.response.*;
import main.api.post.tag.Tag;
import main.api.post.tag.Tags;
import main.api.user.*;
import main.model.PostVote;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewModelFactory {

    @Value("${post.announce.length}")
    private static final int ANNOUNCE_SIZE = 300;

    public static Posts getPosts(Page<main.model.Post> posts, int globalCount, PostModelType pt, UserModelType ut, SimpleDateFormat sdf)
    {
        return createPosts(posts, globalCount, pt, ut, sdf);
    }

    public static PostBehavior getSinglePost(main.model.Post post, SimpleDateFormat sdf)
    {
        return getPostOfType(PostModelType.WITH_COMMENTS, post, getUserOfType(UserModelType.DEFAULT, post
                .getUser()), sdf);
    }

    /**
     * @param posts post from database
     * @param pt    needed post format for response
     * @param ut    needed user format for response
     */
    private static Posts createPosts(Page<main.model.Post> posts, int globalCount, PostModelType pt, UserModelType ut, SimpleDateFormat sdf)
    {
        List<PostBehavior> formattedPosts = new ArrayList<>();
        posts.forEach(post -> {//For each post in the list formats the data for response
            UserBehavior user = getUserOfType(ut, post.getUser());
            PostBehavior p = getPostOfType(pt, post, user, sdf);
            formattedPosts.add(p);
        });
        return new Posts(formattedPosts, globalCount);
    }

    /**
     * @param ut   user format for response
     * @param user user from database that needs to be formatted
     * @return formatted UserBehavior depending on user format.
     */
    private static UserBehavior getUserOfType(UserModelType ut, main.model.User user)
    {
        switch (ut)
        {
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

    public static UserFullInfo getFullInfoUser(main.model.User user, int postsForModeration)
    {
        return new UserFullInfo(user.getId(), user.getName(), user.getPhoto(), user.getEmail(),
                user.isModerator(), postsForModeration, user.isModerator());
    }

    /**
     * @param pt   post format for response
     * @param post post from database
     * @param user user-author of the post
     * @return PostBehavior with needed Post format
     */
    private static PostBehavior getPostOfType(PostModelType pt, main.model.Post post, UserBehavior user, SimpleDateFormat sdf)
    {
        switch (pt)
        {
            case DEFAULT:
                return new Post(
                        post.getId(),
                        sdf.format(post.getTime()),
                        user,
                        post.getTitle(),
                        getAnnounceFromText(post.getText()),
                        (int) post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount()
                );
            case WITH_COMMENTS:
                List<Comment> comments = new ArrayList<>();
                List<String> tags = new ArrayList<>();
                post.getPostComments().forEach(pc -> comments.add(new Comment(pc.getId(), pc.getText(), sdf
                        .format(pc.getTime()), getUserOfType(UserModelType.WITH_PHOTO, pc.getUser()))));
                post.getTags().forEach(tag -> tags.add(tag.getTag().getName()));
                return new PostWithCommentsAndTags(
                        post.getId(),
                        sdf.format(post.getTime()),
                        user,
                        post.getTitle(),
                        post.getText(),
                        (int) post.getPostVotes().stream().filter(PostVote::isValue).count(),
                        (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count(),
                        post.getPostComments().size(),
                        post.getViewCount(),
                        comments,
                        tags);
            case FOR_MODERATION:
                return new PostForModeration(post.getId(), sdf.format(post.getTime()), user, post.getTitle(), Jsoup
                        .parse(post.getText()).text());
            default:
                return null;
        }
    }

    public static Tags getTags(List<main.model.Tag> tags)
    {
        if (tags.isEmpty())
            return new Tags(new ArrayList<Tag>());

        List<Tag> formattedTags = new ArrayList<>();
        double size = 0;
        for (main.model.Tag tag : tags)
        {
            for (main.model.TagToPost ttp : tag.getTaggedPosts())
            {
                if (ttp.getPost().isActive()) size++;
            }
        }
        double finalSize = size;
        double maxWeight = tags.stream().map(tag ->
                tag.getTaggedPosts().stream().filter(ttp -> ttp.getPost().isActive()).count() / finalSize)
                .max(Double::compare).get();
        tags.forEach(tag -> formattedTags.add(new Tag(tag.getName(), ((double) tag.getTaggedPosts().stream()
                .filter(ttp -> ttp.getPost().isActive()).count() / finalSize) / maxWeight)));
        return new Tags(formattedTags);
    }

    private static String getAnnounceFromText(String text)
    {
        String result = Jsoup.parse(text).text();
        return result.length() > ANNOUNCE_SIZE
                ? result.substring(0, ANNOUNCE_SIZE) + "..."
                : result;
    }
}