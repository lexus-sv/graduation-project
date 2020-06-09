package main.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import main.api.post.AddPostRequest;
import main.api.post.PostIdRequest;
import main.api.post.PostModelType;
import main.api.user.UserModelType;
import main.api.post.response.PostWithCommentsAndTags;
import main.api.post.response.Posts;
import main.model.*;
import main.repository.PostRepository;
import main.repository.PostVoteRepository;
import main.repository.TagRepository;
import main.service.AuthServiceImpl;
import main.service.PostService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static main.api.ViewModelFactory.*;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private final AuthServiceImpl authService;

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final PostVoteRepository voteRepository;

    private final static SimpleDateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat dateSRDF = new SimpleDateFormat("dd.MM.yyyy hh:mm");
    private final static SimpleDateFormat defaultDF = new SimpleDateFormat("hh:mm dd.MM.yyyy");

    @Autowired
    public PostServiceImpl(AuthServiceImpl authService, PostRepository postRepository, TagRepository tagRepository, PostVoteRepository voteRepository) {
        this.authService = authService;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public Posts getAll(int offset, int limit, String mode) {
        List<Post> posts = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        posts = getElementsInRange(posts, offset, limit);
        switch (mode) {
            case "recent":
                posts.sort(Comparator.comparing(Post::getTime).reversed());
                break;
            case "popular":
                posts.sort(Comparator.comparing(post -> (post.getPostComments().size())));
                Collections.reverse(posts);
                break;
            case "best":
                posts.sort(Comparator.comparingLong(o -> o.getPostVotes().stream().filter(PostVote::isValue).count()));
                Collections.reverse(posts);
                break;
            case "early":
                posts.sort(Comparator.comparing(Post::getTime));
                break;
        }
        return getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
    }

    @Override
    public Posts search(int offset, int limit, String query) {
        List<Post> posts = query.length() > 0
                ? postRepository.findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(query, ModerationStatus.ACCEPTED, new Date())
                : postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        posts = getElementsInRange(posts, offset, limit);
        return getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT, defaultDF);
    }

    @Override
    public PostWithCommentsAndTags findPostById(int id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setViewCount(post.getViewCount()+1);
            postRepository.save(post);
        }
        return post != null ? (PostWithCommentsAndTags) getSinglePost(post, defaultDF) : null;
    }

    @SneakyThrows
    @Override
    public Posts searchByDate(int offset, int limit, String date) {
        if(isValidDate(date)){
            List<Post> posts = postRepository.findActiveByDate(date);
            posts = getElementsInRange(posts, offset, limit);

            return getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
        }
        return null;
    }

    @Override
    public Posts searchByTag(int offset, int limit, String tagName) {
        List<Post> posts = postRepository.findAllByTag(tagName.trim());
        posts = getElementsInRange(posts, offset, limit);
        return getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT, dateSRDF);
    }

    @Override
    public Posts getPostsForModeration(int offset, int limit, String status) {
//        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
//        if (user != null) {
//            ModerationStatus moderationStatus = ModerationStatus.getEqualStatus(status);
//            List<Post> posts = postRepository.findAllByActiveTrueAndModeratorOrModerationStatusAndActiveTrue(user, moderationStatus);
//            posts = getElementsInRange(posts, offset, limit);
//            return getPosts(posts, PostModelType.FOR_MODERATION, UserModelType.DEFAULT, dateSRDF);
//        }
        return null;
    }

    @Override
    public Posts getMyPosts(int offset, int limit, String status) {
//        List<Post> posts = new ArrayList<>();
//        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
//        if (user != null) {
//            switch (status) {
//                case "inactive":
//                    posts = postRepository.findAllByActiveFalse();
//                    break;
//                case "pending":
//                    posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.NEW);
//                    break;
//                case "declined":
//                    posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.DECLINED);
//                    break;
//                case "published":
//                    posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.ACCEPTED);
//                    break;
//            }
//            posts = getElementsInRange(posts, offset, limit);
//            return getPosts(posts, PostModelType.DEFAULT, UserModelType.WITH_EMAIL, dateSRDF);
//        }
        return null;
    }

    @Override
    public HashMap<Object, Object> add(AddPostRequest request) {
        HashMap<Object, Object> response = new HashMap<>();
        response.put("result", false);
        JSONObject errors = new JSONObject();
        response.put("errors", errors);

        int textLength = request.getText().trim().length();
        int titleLength = request.getTitle().trim().length();
        if (titleLength < 10) {
            errors.put("title", "Заголовок должен быть не меньше 10 символов");
        }
        if (textLength < 500) {
            errors.put("text", "Текст поста должен быть не менее 500 символов");
        }
        if (errors.isEmpty()) {
            response.remove("errors");
            response.put("result", true);
            Post post = new Post();
            post.setActive(request.isActive());
            post.setModerationStatus(ModerationStatus.NEW);
            post.setText(request.getText());
            post.setTitle(request.getTitle());
            try {
                post.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(request.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            post.setViewCount(0);
            post.setModerator(null);
//            post.setUser(authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId()));
            List<TagToPost> tags = new ArrayList<>();
            request.getTags().forEach(tag -> {
                TagToPost ttp = new TagToPost();
                ttp.setPost(post);
                if(!tagRepository.existsByName(tag)){
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
        return response;
    }

    @Override
    public HashMap<Object, Object> edit(int id, AddPostRequest request) {
        HashMap<Object, Object> response = new HashMap<>();
        response.put("result", false);
        JSONObject errors = new JSONObject();
        response.put("errors", errors);

        int textLength = request.getText().trim().length();
        int titleLength = request.getTitle().trim().length();
        if (titleLength < 10) {
            errors.put("title", "Заголовок должен быть не меньше 10 символов");
        }
        if (textLength < 200) {
            errors.put("text", "Текст поста должен быть не менее 500 символов");
        }
        if (errors.isEmpty()) {
            response.remove("errors");
            response.put("result", true);
            Post post = postRepository.findById(id).orElse(null);
            post.setActive(request.isActive());
            post.setModerationStatus(ModerationStatus.NEW);
            post.setText(request.getText());
            post.setTitle(request.getTitle());
            try {
                post.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(request.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<TagToPost> tags = new ArrayList<>();
            request.getTags().forEach(tag -> {
                TagToPost ttp = new TagToPost();
                ttp.setPost(post);
                if(!tagRepository.existsByName(tag)){
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
        return response;
    }

    @Override
    public HashMap<String, Boolean> like(PostIdRequest request) {
        return makeVote(request, true);
    }

    @Override
    public HashMap<String, Boolean> dislike(PostIdRequest request) {
        return makeVote(request, false);
    }

    private HashMap<String, Boolean> makeVote(PostIdRequest request, boolean value) {//value = true - like, value = false - dislike
        HashMap<String, Boolean> response = new HashMap<>();
        response.put("result", false);
        Post post = postRepository.findById(request.getPostId()).orElse(null);
//        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
//        if (user != null) {
//            PostVote existingVote = voteRepository.findByUserAndPost(user, post).orElse(null);
//            if (existingVote == null) {
//                PostVote vote = new PostVote();
//                vote.setValue(value);
//                vote.setUser(user);
//                vote.setPost(post);
//                vote.setTime(new Date());
//                voteRepository.save(vote);
//                log.info("IN makeVote vote: {} saved, value: {}", vote, value);
//                response.put("result", true);
//                log.info("IN makeVote response: {}", response);
//                return response;
//            } else {//if vote for post with user already exists
//                if(existingVote.isValue() == value){
//                    return response;
//                } else {
//                    existingVote.setValue(value);
//                    voteRepository.save(existingVote);
//                    log.info("IN makeVote vote: {} saved, value inverted to: {}", existingVote, value);
//                    response.put("result", true);
//                }
//            }
//            log.info("final response: {}", response);
//            return response;
//        }
        return null;
    }


    private List<Post> getElementsInRange(List<Post> list, int offset, int limit) {
        int lastElementIndex = offset + limit;
        int lastPostIndex = list.size();
        if (lastPostIndex >= offset) {//если есть элементы входящие в нужный диапазон
            if (lastElementIndex <= lastPostIndex) {//если все элементы с нужными индексами есть в листе
                return list.subList(offset, lastElementIndex);
            } else {//если не хватает элементов, то в посты записываем остаток, считая от offset
                return list.subList(offset, lastPostIndex);
            }
        } else {
            return new ArrayList<>();
        }
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
