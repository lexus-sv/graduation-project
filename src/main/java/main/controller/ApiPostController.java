package main.controller;

import main.model.*;
import main.model.request.AddPostRequest;
import main.model.response.PostModelType;
import main.model.response.UserModelType;
import main.model.response.ViewModelFactory;
import main.model.response.post.PostBehavior;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.UserRepository;
import main.service.AuthService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ApiPostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AuthService authService;

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity<?> getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        System.out.println(RequestContextHolder.currentRequestAttributes().getSessionId());
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
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
    public ResponseEntity searchPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "query") String query) {
        List<Post> posts;
        if (query.length() != 0) {
            posts = postRepository.findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(query, ModerationStatus.ACCEPTED, new Date());
        } else {
            posts = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        }
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }


    @GetMapping(value = "/api/post/{id}")
    public ResponseEntity getPostById(@PathVariable int id) {
        //Здесь должно быть условие с активностью поста и его статусом модерации
        Optional<Post> post = postRepository.findById(id);
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
        List<Post> posts;
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        Date fromDate = getDateOrNull(stringDate);
        if (fromDate != null) {
            calendar.setTime(fromDate);
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) - 1);
            fromDate = calendar.getTime();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) - 1);
            Date toDate = calendar.getTime();
            posts = postRepository.findAllByTimeAfterAndTimeBeforeAndActiveTrueAndModerationStatus(fromDate, toDate, ModerationStatus.ACCEPTED);
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
    ) {
        List<Post> posts = postRepository.findAllByTag(tag.trim(), ModerationStatus.ACCEPTED);
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior postBehavior = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(postBehavior, HttpStatus.OK);
    }

    @GetMapping("/api/post/moderation")
    public ResponseEntity<?> moderation(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status
    ) {
        List<Post> posts;
        ModerationStatus moderationStatus;
        switch (status) {
            case "new":
                moderationStatus = ModerationStatus.NEW;
                break;
            case "declined":
                moderationStatus = ModerationStatus.DECLINED;
                break;
            default:
                moderationStatus = ModerationStatus.ACCEPTED;
        }
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        posts = postRepository.findAllByActiveTrueAndModeratorOrModerationStatusAndActiveTrue(user, moderationStatus);
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.FOR_MODERATION, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping("/api/post/my")
    public ResponseEntity my(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status) {
        //inactive - скрытые, ещё не опубликованы (is_active = 0)
        //pending - активные, ожидают утверждения модератором (is_active = 1,
        //moderation_status = NEW)
        //declined - отклонённые по итогам модерации (is_active = 1, moderation_status =
        //DECLINED)
        //published - опубликованные по итогам модерации (is_active = 1, moderation_status =
        //ACCEPTED)
        List<Post> posts = new ArrayList<>();
        switch (status) {
            case "inactive":
                posts = postRepository.findAllByActiveFalse();
                break;
            case "pending":
                posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.NEW);
                break;
            case "declined":
                posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.DECLINED);
                break;
            case "published":
                posts = postRepository.findAllByActiveTrueAndModerationStatus(ModerationStatus.ACCEPTED);
                break;
        }
        posts = getElementsInRange(posts, offset, limit);
        PostBehavior responseBody = ViewModelFactory.getPosts(posts, PostModelType.DEFAULT, UserModelType.WITH_EMAIL);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @PostMapping("/api/post")
    public ResponseEntity<?> post(@RequestBody AddPostRequest request) throws ParseException {
        HashMap<Object, Object> response = new HashMap<>();
        response.put("result", false);
        JSONObject errors = new JSONObject();
        response.put("errors", errors);

        int textLength = request.getText().trim().length();
        int titleLength = request.getTitle().trim().length();
        if (titleLength < 10){
            errors.put("title", "Заголовок должен быть не меньше 10 символов");
        }
        if(textLength<500){
            errors.put("text", "Текст поста должен быть не менее 500 символов");
        }
        if(errors.isEmpty()){
            response.remove("errors");
            response.put("result", true);
            Post post = new Post();
            post.setActive(request.isActive());
            post.setModerationStatus(ModerationStatus.NEW);
            post.setText(request.getText());
            post.setTitle(request.getTitle());
            post.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(request.getTime()));
            post.setViewCount(0);
            post.setModerator(null);
            post.setUser(authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId()));
            List<TagToPost> tags = new ArrayList<>();
            request.getTags().forEach(tag->{
                TagToPost ttp = new TagToPost();
                ttp.setPost(post);
                ttp.setTag(tagRepository.findFirstByName(tag));
                tags.add(ttp);
            });
            post.setTags(tags);
            postRepository.save(post);
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }

    /**
     * @param list   list that will be cut to specific range
     * @param offset begin index
     * @param limit  amount of elements
     * @return sublist
     */
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

    private Date getDateOrNull(String s) {
        try {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(s);
        } catch (ParseException ex) {
            return null;
        }
    }
}
