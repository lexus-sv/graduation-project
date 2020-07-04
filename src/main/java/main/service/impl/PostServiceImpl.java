package main.service.impl;

import static main.api.ViewModelFactory.getPosts;
import static main.api.ViewModelFactory.getSinglePost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import main.api.post.AddPostRequest;
import main.api.post.PostAddResponse;
import main.api.post.PostIdRequest;
import main.api.post.PostModelType;
import main.api.post.PostResponseErrors;
import main.api.post.response.Posts;
import main.api.user.UserModelType;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.model.Tag;
import main.model.TagToPost;
import main.model.User;
import main.repository.PostRepository;
import main.repository.PostVoteRepository;
import main.repository.TagRepository;
import main.service.PostService;
import main.service.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

  @Value("${post.title.minLength}")
  private int postTitleLength;
  @Value("${post.text.minLength}")
  private int postTextLength;

  private final PostRepository postRepository;

  private final TagRepository tagRepository;

  private final PostVoteRepository voteRepository;

  private final Settings settings;

  private final static SimpleDateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private final static SimpleDateFormat dateSRDF = new SimpleDateFormat("dd.MM.yyyy hh:mm");
  private final static SimpleDateFormat defaultDF = new SimpleDateFormat("hh:mm dd.MM.yyyy");
  private final String PREMODERATION_KEY = "POST_PREMODERATION";

  @Autowired
  public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository,
      PostVoteRepository voteRepository, Settings settings) {
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.voteRepository = voteRepository;
    this.settings = settings;
  }

  @Override
  public Posts getAll(int offset, int limit, String mode) {
    int count = postRepository
        .countAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
            new Date());
    switch (mode) {
      case "recent":
        return getRecent(offset, limit, count);
      case "popular":
        return getPosts(postRepository.findPopular(ModerationStatus.ACCEPTED, new Date(),
            PageRequest.of(offset / limit, limit)), count, PostModelType.DEFAULT,
            UserModelType.DEFAULT, defaultDF);
      case "best":
        count = postRepository.countBest(ModerationStatus.ACCEPTED, new Date()).size();
        return getPosts(postRepository
                .findBest(ModerationStatus.ACCEPTED, new Date(), PageRequest.of(offset / limit, limit)),
            count, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
      case "early":
        return getPosts(postRepository
                .findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
                    new Date(),
                    PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.ASC, "time"))),
            count, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
    }
    return null;
  }

  private Posts getRecent(int offset, int limit, int count) {
    return getPosts(postRepository
            .findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
                new Date(),
                PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "time"))),
        count, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
  }

  @Override
  public Posts search(int offset, int limit, String query) {
    int count = 0;
    if (query.length() > 0) {
      count = postRepository
          .countAllByTitleContainingOrTextContainingAndModerationStatusAndTimeBeforeAndActiveTrue(
              query, query, ModerationStatus.ACCEPTED, new Date());
      return getPosts(postRepository
              .findAllByTitleContainingOrTextContainingAndModerationStatusAndTimeBeforeAndActiveTrue(
                  query, query, ModerationStatus.ACCEPTED, new Date(), PageRequest
                      .of(offset / limit, limit)), count, PostModelType.DEFAULT, UserModelType.DEFAULT,
          defaultDF);
    } else {
      count = postRepository
          .countAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
              new Date());
      return getPosts(postRepository
              .findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
                  new Date(), PageRequest
                      .of(offset / limit, limit)), count, PostModelType.DEFAULT, UserModelType.DEFAULT,
          defaultDF);
    }
  }

  @Override
  public main.api.post.response.Post findPostById(int id) {
    Post post = postRepository
        .findById(id)
        .orElse(null);
    if (post == null) {
      throw new NullPointerException("Post with id " + id + " was not found");
    }
    post.setViewCount(post.getViewCount() + 1);
    postRepository.save(post);
    return getSinglePost(post, defaultDF);
  }

  @SneakyThrows
  @Override
  public Posts searchByDate(int offset, int limit, String date) {
    if (!isValidDate(date)) {
      throw new IllegalArgumentException("Invalid date");
    }

    int count = postRepository.countByDate(date);
    Page<Post> posts = postRepository.findActiveByDate(date, PageRequest.of(offset / limit, limit));
    return getPosts(posts, count, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
  }

  @Override
  public Posts searchByTag(int offset, int limit, String tagName) {
    int count = postRepository.countByTagName(tagName.toLowerCase());
    Page<Post> posts = postRepository
        .findAllByTag(tagName.trim(), PageRequest.of(offset / limit, limit));
    return getPosts(posts, count, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
  }

  @Override
  public Posts getPostsForModeration(int offset, int limit, String status, User user) {
    ModerationStatus moderationStatus = ModerationStatus.getEqualStatus(status);
    int count = postRepository.countByModerationStatusAndActiveTrue(moderationStatus);
    Page<Post> posts = postRepository
        .findPostsForModeration(user, moderationStatus, PageRequest.of(offset / limit, limit));
    return getPosts(posts, count, PostModelType.FOR_MODERATION, UserModelType.DEFAULT, dateSRDF);
  }

  @Override
  public Posts getMyPosts(int offset, int limit, String status, User user) {
    Pageable pageable = PageRequest.of(offset / limit, limit);
    int count = 0;
    switch (status) {
      case "inactive":
        count = postRepository.countAllByActiveFalseAndUser(user);
        return getPosts(postRepository.findAllByActiveFalseAndUser(user, pageable), count,
            PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
      case "pending":
        count = postRepository
            .countAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.NEW, new Date());
        return getPosts(postRepository
                .findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.NEW, pageable),
            count, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
      case "declined":
        count = postRepository
            .countAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.DECLINED,
                new Date());
        return getPosts(postRepository
            .findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.DECLINED,
                pageable), count, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
      case "published":
        count = postRepository
            .countAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED,
                new Date());
        return getPosts(postRepository
            .findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.ACCEPTED,
                pageable), count, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
    }
    throw new IllegalArgumentException("wrong status");
  }

  @Override
  public PostAddResponse add(AddPostRequest request, User user) {
    PostAddResponse response = new PostAddResponse();
    PostResponseErrors errors = new PostResponseErrors();

    int textLength = request.getText().trim().length();
    int titleLength = request.getTitle().trim().length();
    System.out.println(postTextLength + " " + postTitleLength);
    if (titleLength < postTitleLength) {
      response.setResult(false);
      errors.setTitle("Заголовок должен быть не меньше" + postTitleLength + "символов");
    }
    if (textLength < postTextLength) {
      response.setResult(false);
      errors.setText("Текст поста должен быть не менее" + postTextLength + "символов");
    }
    if (errors.getText() == null && errors.getTitle() == null) {
      response.setResult(true);
      Post post = new Post();
      post.setActive(request.isActive());

      post.setModerationStatus(
          settings.getSetting(PREMODERATION_KEY)
              ? ModerationStatus.NEW
              : ModerationStatus.ACCEPTED);

      post.setText(request.getText());
      log.info("IN addPost text: {}", request.getText());
      post.setTitle(request.getTitle());
      try {
        post.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(request.getTime()));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      post.setViewCount(0);
      post.setModerator(null);
      log.info("IN addPost post has user {}", user);
      post.setUser(user);
      addTagsAndSave(request, post);
    } else {
      response.setErrors(errors);
    }
    return response;
  }

  @Override
  public PostAddResponse edit(int id, AddPostRequest request) {
    PostAddResponse response = new PostAddResponse();
    PostResponseErrors errors = new PostResponseErrors();

    int textLength = request.getText().trim().length();
    int titleLength = request.getTitle().trim().length();
    if (titleLength < postTitleLength) {
      response.setResult(false);
      errors.setTitle("Заголовок должен быть не меньше" + postTitleLength + " символов");
    }
    if (textLength < postTextLength) {
      response.setResult(false);
      errors.setText("Текст поста должен быть не менее" + postTextLength + "символов");
    }
    if (errors.getText() != null && errors.getTitle() != null) {
      response.setResult(true);
      Post post = postRepository.findById(id).orElse(null);
      post.setActive(request.isActive());

      post.setModerationStatus(
          settings.getSetting(PREMODERATION_KEY)
              ? ModerationStatus.NEW
              : ModerationStatus.ACCEPTED);

      post.setText(request.getText());
      post.setTitle(request.getTitle());
      try {
        post.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(request.getTime()));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      addTagsAndSave(request, post);
    } else {
      response.setErrors(errors);
    }
    return response;
  }

  @Override
  public HashMap<String, Boolean> like(PostIdRequest request, User user) {
    return makeVote(request, true, user);
  }

  @Override
  public HashMap<String, Boolean> dislike(PostIdRequest request, User user) {
    return makeVote(request, false, user);
  }

  private HashMap<String, Boolean> makeVote(PostIdRequest request, boolean value,
      User user) {//value = true - like, value = false - dislike
    HashMap<String, Boolean> response = new HashMap<>();
    response.put("result", false);
    Post post = postRepository.findById(request.getPostId()).orElse(null);
    PostVote existingVote = voteRepository.findByUserAndPost(user, post).orElse(null);
    if (existingVote == null) {
      PostVote vote = new PostVote();
      vote.setValue(value);
      vote.setUser(user);
      vote.setPost(post);
      vote.setTime(new Date());
      voteRepository.save(vote);
      log.info("IN makeVote vote: {} saved, value: {}", vote, value);
      response.put("result", true);
      log.info("IN makeVote response: {}", response);
      return response;
    } else {//if vote for post with user already exists
      if (existingVote.isValue() == value) {
        return response;
      } else {
        existingVote.setValue(value);
        voteRepository.save(existingVote);
        log.info("IN makeVote vote: {} saved, value inverted to: {}", existingVote, value);
        response.put("result", true);
      }
    }
    log.info("final response: {}", response);
    return response;
  }


  private void addTagsAndSave(AddPostRequest request, Post post) {
    List<TagToPost> tags = new ArrayList<>();
    request
        .getTags()
        .forEach(tag -> {
          TagToPost ttp = new TagToPost();
          ttp.setPost(post);
          if (!tagRepository.existsByName(tag)) {
            Tag t = new Tag();
            t.setName(tag);
            tagRepository.save(t);
          }
          ttp.setTag(tagRepository.findFirstByName(tag));
          tags.add(ttp);
        });
    post.setTags(tags);
    postRepository.save(post);
  }

  private boolean isValidDate(String date) {
    try {
      searchDateFormat.parse(date);
      return true;
    } catch (ParseException ex) {
      return false;
    }
  }
}
