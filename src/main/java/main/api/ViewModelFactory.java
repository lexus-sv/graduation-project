package main.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import main.api.post.PostModelType;
import main.api.post.comment.Comment;
import main.api.post.response.Post;
import main.api.post.response.Posts;
import main.api.post.tag.Tag;
import main.api.post.tag.Tags;
import main.api.user.UserFullInfo;
import main.api.user.UserModelType;
import main.model.ModerationStatus;
import main.model.PostVote;
import main.model.TagToPost;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

public class ViewModelFactory {

  @Value("${post.announce.length}")
  private static final int ANNOUNCE_SIZE = 300;

  public static Posts getPosts(Page<main.model.Post> posts, int globalCount, PostModelType pt,
      UserModelType ut, SimpleDateFormat sdf) {
    return createPosts(posts, globalCount, pt, ut, sdf);
  }

  public static Post getSinglePost(main.model.Post post, SimpleDateFormat sdf) {
    return getPostOfType(PostModelType.WITH_COMMENTS, post,
        getUserOfType(UserModelType.DEFAULT, post
            .getUser()), sdf);
  }

  /**
   * @param posts post from database
   * @param pt    needed post format for response
   * @param ut    needed user format for response
   */
  private static Posts createPosts(Page<main.model.Post> posts, int globalCount, PostModelType pt,
      UserModelType ut, SimpleDateFormat sdf) {
    List<Post> formattedPosts = new ArrayList<>();
    posts.forEach(post -> {//For each post in the list formats the data for response
      UserFullInfo user = getUserOfType(ut, post.getUser());
      Post p = getPostOfType(pt, post, user, sdf);
      formattedPosts.add(p);
    });
    return new Posts(formattedPosts, globalCount);
  }

  /**
   * @param ut   user format for response
   * @param user user from database that needs to be formatted
   * @return formatted UserBehavior depending on user format.
   */
  private static UserFullInfo getUserOfType(UserModelType ut, main.model.User user) {
    switch (ut) {
      case DEFAULT:
        return UserFullInfo.builder()
            .id(user.getId())
            .name(user.getName()).build();
      case FULL_INFO:
        return UserFullInfo.builder()
            .id(user.getId())
            .name(user.getName())
            .photo(user.getPhoto())
            .email(user.getEmail())
            .moderation(user.isModerator())
            .moderationCount(user.getModeratedPosts().size()).build();
      case WITH_PHOTO:
        return UserFullInfo.builder()
            .id(user.getId())
            .name(user.getName())
            .photo(user.getPhoto()).build();
      case WITH_EMAIL:
        return UserFullInfo.builder()
            .id(user.getId())
            .email(user.getEmail()).build();
      default:
        return UserFullInfo.builder().build();
    }
  }

  public static UserFullInfo getFullInfoUser(main.model.User user, int postsForModeration) {
    return UserFullInfo.builder()
        .id(user.getId())
        .name(user.getName())
        .photo(user.getPhoto())
        .email(user.getEmail())
        .moderation(user.isModerator())
        .moderationCount(postsForModeration).build();
  }

  /**
   * @param pt   post format for response
   * @param post post from database
   * @param user user-author of the post
   * @return PostBehavior with needed Post format
   */
  private static Post getPostOfType(PostModelType pt, main.model.Post post, UserFullInfo user,
      SimpleDateFormat sdf) {
    switch (pt) {
      case DEFAULT:
        return Post.builder()
            .id(post.getId())
            .time(sdf.format(post.getTime()))
            .user(user)
            .title(post.getTitle())
            .announce(getAnnounceFromText(post.getText()))
            .likeCount((int) post.getPostVotes().stream().filter(PostVote::isValue).count())
            .dislikeCount(
                (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count())
            .commentCount(post.getPostComments().size())
            .viewCount(post.getViewCount())
            .build();
      case WITH_COMMENTS:
        List<Comment> comments = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        post.getPostComments().forEach(pc -> comments.add(new Comment(pc.getId(), pc.getText(), sdf
            .format(pc.getTime()), getUserOfType(UserModelType.WITH_PHOTO, pc.getUser()))));
        post.getTags().forEach(tag -> tags.add(tag.getTag().getName()));
        return Post.builder()
            .id(post.getId())
            .time(sdf.format(post.getTime()))
            .user(user)
            .title(post.getTitle())
            .text(post.getText())
            .likeCount((int) post.getPostVotes().stream().filter(PostVote::isValue).count())
            .dislikeCount(
                (int) post.getPostVotes().stream().filter(vote -> !vote.isValue()).count())
            .commentCount(post.getPostComments().size())
            .viewCount(post.getViewCount())
            .comments(comments)
            .tags(tags).build();

      case FOR_MODERATION:
        return Post.builder()
            .id(post.getId())
            .time(sdf.format(post.getTime()))
            .user(user)
            .title(post.getTitle())
            .announce(Jsoup.parse(post.getText()).text())
            .build();
      default:
        return Post.builder().build();
    }
  }

  public static Tags getTags(List<main.model.Tag> tags) {
    if (tags.isEmpty()) {
      return new Tags(new ArrayList<>());
    }

    List<Tag> formattedTags = new ArrayList<>();
    double size = 0;
    for (main.model.Tag tag : tags) {
      for (main.model.TagToPost ttp : tag.getTaggedPosts()) {
        if (ttp.getPost().isActive()
            && ttp.getPost().getModerationStatus() == ModerationStatus.ACCEPTED) {
          size++;
        }
      }
    }
    double finalSize = size;
    double maxWeight = tags.stream()
        .map(tag -> tag.getTaggedPosts().stream()
            .map(TagToPost::getPost)
            .filter(
                post -> post.isActive() && post.getModerationStatus() == ModerationStatus.ACCEPTED)
            .count())
        .map(weight -> weight / finalSize)
        .max(Double::compare)
        .orElse(0.0);
    tags.forEach(
        tag -> formattedTags.add(new Tag(tag.getName(),
            ((double) tag.getTaggedPosts().stream()
                .filter(ttp -> ttp.getPost().isActive()
                    && ttp.getPost().getModerationStatus() == ModerationStatus.ACCEPTED).count()
                / finalSize) / maxWeight)));
    return new Tags(formattedTags);
  }

  private static String getAnnounceFromText(String text) {
    String result = Jsoup.parse(text).text();
    return result.length() > ANNOUNCE_SIZE
        ? result.substring(0, ANNOUNCE_SIZE) + "..."
        : result;
  }
}