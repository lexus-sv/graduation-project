package main.controller;

import main.InitInfo;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.model.response.PostGetModel;
import main.model.response.PostModelType;
import main.model.response.UserModelType;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ApiGeneralController {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/init")
    public ResponseEntity<InitInfo> init() {
        return new ResponseEntity(new InitInfo(), HttpStatus.OK);
    }


    //offset - сдвиг от 0 для постраничного вывода
    //limit - количество постов, которое надо вывести
    //mode - режим вывода (сортировка):
    //recent - сортировать по дате публикации, выводить сначала новые
    //popular - сортировать по убыванию количества комментариев
    //best - сортировать по убыванию количества лайков
    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        System.out.println(mode + offset + limit);
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postsIterable = postRepository.findAll();
        postsIterable.forEach(posts::add);
        posts = posts.stream().filter(post -> post.isActive() &&
                post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
                post.getTime().before(new Date())).collect(Collectors.toList());
        switch (mode) {
            case "recent":
                posts.sort(Comparator.comparing(Post::getTime).reversed());
            case "popular":
                posts.sort(Comparator.comparing(post -> (post.getPostComments().size())));
        }
        PostGetModel responseBody = new PostGetModel(posts.size(), posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

}
