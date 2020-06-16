package main.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import main.api.post.*;
import main.api.post.response.PostWithCommentsAndTags;
import main.api.post.response.Posts;
import main.api.user.UserModelType;
import main.model.*;
import main.repository.PostRepository;
import main.repository.PostVoteRepository;
import main.repository.TagRepository;
import main.service.PostService;
import main.service.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static main.api.ViewModelFactory.getPosts;
import static main.api.ViewModelFactory.getSinglePost;

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
    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, PostVoteRepository voteRepository, Settings settings) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.voteRepository = voteRepository;
        this.settings = settings;
    }

    @Override
    public Posts getAll(int offset, int limit, String mode) {
        List<Post> posts = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        switch (mode) {
            case "recent":
                posts.sort(Comparator
                        .comparing(Post::getTime)
                        .reversed());
                break;
            case "popular":
                posts.sort(Comparator.comparing(post -> (post
                        .getPostComments()
                        .size())));
                Collections.reverse(posts);
                break;
            case "best":
                posts.sort(Comparator.comparingLong(o -> o
                        .getPostVotes()
                        .stream()
                        .filter(PostVote::isValue)
                        .count()));
                Collections.reverse(posts);
                break;
            case "early":
                posts.sort(Comparator.comparing(Post::getTime));
                break;
        }
        log.info("posts: {}", posts);
        return getPosts(posts,  offset, limit, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
    }

    @Override
    public Posts search(int offset, int limit, String query) {
        List<Post> posts = query.length() > 0
                ? postRepository.findAllByTitleContainingOrTextContainingAndModerationStatusAndTimeBeforeAndActiveTrue(query, query, ModerationStatus.ACCEPTED, new Date())
                : postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        return getPosts(posts, offset, limit, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
    }

    @Override
    public PostWithCommentsAndTags findPostById(int id) {
        Post post = postRepository
                .findById(id)
                .orElse(null);
        if (post == null)
            throw new NullPointerException("Post with id " + id + " was not found");
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return (PostWithCommentsAndTags) getSinglePost(post, defaultDF);
    }

    @SneakyThrows
    @Override
    public Posts searchByDate(int offset, int limit, String date) {
        if (!isValidDate(date))
            throw new IllegalArgumentException("Invalid date");

        List<Post> posts = postRepository.findActiveByDate(date);
        return getPosts(posts, limit, offset, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
    }

    @Override
    public Posts searchByTag(int offset, int limit, String tagName) {
        List<Post> posts = postRepository.findAllByTag(tagName.trim());
        return getPosts(posts, offset, limit, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
    }

    @Override
    public Posts getPostsForModeration(int offset, int limit, String status, User user) {
        ModerationStatus moderationStatus = ModerationStatus.getEqualStatus(status);
        List<Post> posts = postRepository.findPostsForModeration(user, moderationStatus);
        return getPosts(posts, offset, limit, PostModelType.FOR_MODERATION, UserModelType.DEFAULT, dateSRDF);
    }

    @Override
    public Posts getMyPosts(int offset, int limit, String status, User user) {
        List<Post> posts = new ArrayList<>();
        switch (status) {
            case "inactive":
                posts = postRepository.findAllByActiveFalseAndUser(user);
                break;
            case "pending":
                posts = postRepository.findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.NEW);
                break;
            case "declined":
                posts = postRepository.findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.DECLINED);
                break;
            case "published":
                posts = postRepository.findAllByActiveTrueAndUserAndModerationStatus(user, ModerationStatus.ACCEPTED);
                break;
        }
        return getPosts(posts, offset, limit, PostModelType.DEFAULT, UserModelType.WITH_EMAIL, dateSRDF);
    }

    @Override
    public PostAddResponse add(AddPostRequest request, User user) {
        PostAddResponse response = new PostAddResponse();
        PostResponseErrors errors = new PostResponseErrors();

        int textLength = request.getText().trim().length();
        int titleLength = request.getTitle().trim().length();
        System.out.println(postTextLength + " " +postTitleLength);
        if (titleLength < postTitleLength) {
            response.setResult(false);
            errors.setTitle("Заголовок должен быть не меньше"+postTitleLength+ "символов");
        }
        if (textLength < postTextLength) {
            response.setResult(false);
            errors.setText("Текст поста должен быть не менее"+postTextLength+"символов");
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
            errors.setTitle("Заголовок должен быть не меньше"+postTitleLength+" символов");
        }
        if (textLength < postTextLength) {
            response.setResult(false);
            errors.setText("Текст поста должен быть не менее"+ postTextLength+ "символов");
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

    private HashMap<String, Boolean> makeVote(PostIdRequest request, boolean value, User user) {//value = true - like, value = false - dislike
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
