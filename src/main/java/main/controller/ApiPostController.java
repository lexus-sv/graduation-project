package main.controller;

import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.model.User;
import main.model.response.PostModelType;
import main.model.response.UserModelType;
import main.model.response.ViewModelFactory;
import main.model.response.post.PostBehavior;
import main.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ApiPostController {

    @Autowired
    private PostRepository postRepository;

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity<?> getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        System.out.println(RequestContextHolder.currentRequestAttributes().getSessionId());
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postsIterable = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        postsIterable.forEach(posts::add);
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
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
    public ResponseEntity searchPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "query") String query) {
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postIterable;
        if (query.length() != 0) {
            postIterable = postRepository.findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(query, ModerationStatus.ACCEPTED, new Date());
        } else {
            postIterable = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        }
        postIterable.forEach(posts::add);
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }


    @GetMapping(value = "/api/post/{id}")
    public ResponseEntity getPostById(@PathVariable int id) {
        Optional<Post> post = postRepository.findByIdAndActiveTrueAndModerationStatus(id, ModerationStatus.ACCEPTED);
        if (post.isPresent()) {
            PostBehavior responseBody = ViewModelFactory.getSinglePost(post.get());
            return new ResponseEntity(responseBody, HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/post/byDate")
    public ResponseEntity<?> getPostsByDate(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "date") String stringDate
    ) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<Post> posts = new ArrayList<>();
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        Date fromDate = getDateOrNull(stringDate);
        if(fromDate!=null) {
            calendar.setTime(fromDate);
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)-1);
            fromDate = calendar.getTime();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)-1);
            Date toDate = calendar.getTime();
            Iterable<Post> postIterable = postRepository.findAllByTimeAfterAndTimeBeforeAndActiveTrueAndModerationStatus(fromDate, toDate, ModerationStatus.ACCEPTED);
            postIterable.forEach(posts::add);
            posts = getElementsInRange(posts, offset, limit);
            PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
            return new ResponseEntity(responseBody, HttpStatus.OK);
        } else {
            System.out.println("Bad request");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/post/byTag")
    public ResponseEntity<?> getPostsByTag(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "tag") String tag
    )
    {
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postIterable = postRepository.findAllByTag(tag.trim(), ModerationStatus.ACCEPTED);
        postIterable.forEach(posts::add);
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior postBehavior = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(postBehavior, HttpStatus.OK);
    }

    @GetMapping("/api/post/moderation")
    public ResponseEntity<?> moderation(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status
    )
    {
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postIterable = null;
        ModerationStatus moderationStatus;
        switch (status){
            case "new":
                moderationStatus=ModerationStatus.NEW;
                break;
            case "accepted":
                moderationStatus=ModerationStatus.ACCEPTED;
                break;
            case "declined":
                moderationStatus=ModerationStatus.DECLINED;
                break;
        }
        return null;
    }

    /**
     * @param list   list that will be cut to specific range
     * @param offset begin index
     * @param limit  amount of elements
     * @return sublist
     */
    private List getElementsInRange(List list, int offset, int limit) {
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

    private Date getDateOrNull(String s) {
        try {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(s);
        } catch (ParseException ex) {
            return null;
        }
    }
}
