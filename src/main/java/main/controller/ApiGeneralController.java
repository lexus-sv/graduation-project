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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLOutput;
import java.util.*;
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
    //early - сортировать по дате публикации, выводить сначала старые
    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity<?> getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postsIterable = postRepository.findAllByActiveTrueAndModerationStatusAndTimeBefore(ModerationStatus.ACCEPTED, new Date());
        postsIterable.forEach(posts::add);
        posts = getElementsInRange(posts, offset, limit);
        switch (mode) {
            case "recent":
                posts.sort(Comparator.comparing(Post::getTime).reversed());
            case "popular":
                posts.sort(Comparator.comparing(post -> (post.getPostComments().size())));
                Collections.reverse(posts);
            case "best":
                posts.sort(Comparator.comparingLong(o -> o.getPostVotes().stream().filter(PostVote::isValue).count()));
                Collections.reverse(posts);
            case "early":
                posts.sort(Comparator.comparing(Post::getTime));
        }
        PostGetModel responseBody = new PostGetModel(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/search", params = {"offset","limit","query"})
    public ResponseEntity searchPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "query") String query)
    {
        System.out.println("query = "+query);
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postIterable = postRepository.findAllByTitleContainingAndModerationStatusAndTimeBeforeAndActiveTrue(query, ModerationStatus.ACCEPTED, new Date());
        postIterable.forEach(posts::add);
        posts = getElementsInRange(posts, offset, limit);
        PostGetModel responseBody = new PostGetModel(posts, PostModelType.DEFAULT, UserModelType.DEFAULT);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    /**
     *
     * @param list list that will be cut to specific range
     * @param offset begin index
     * @param limit amount of elements
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
}