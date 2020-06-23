package main.controller;

import main.api.post.AddPostRequest;
import main.api.post.PostAddResponse;
import main.api.post.PostIdRequest;
import main.api.post.response.PostWithCommentsAndTags;
import main.api.post.response.Posts;
import main.service.AuthService;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;

@RestController
public class ApiPostController {

    private final PostService postService;
    private final AuthService authService;

    @Autowired
    public ApiPostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity<Posts> getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        return ResponseEntity.ok(postService.getAll(offset, limit, mode));
    }

    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
    public ResponseEntity<Posts> searchPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "query") String query) {
        return ResponseEntity.ok(postService.search(offset, limit, query));
    }


    @GetMapping(value = "/api/post/{id}")
    public ResponseEntity<PostWithCommentsAndTags> getPostById(@PathVariable int id) {
        return ResponseEntity.ok(postService.findPostById(id));
    }

    @GetMapping("/api/post/byDate")
    public ResponseEntity<Posts> getPostsByDate(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "date") String date
    ) {
        return ResponseEntity.ok(postService.searchByDate(offset, limit, date));
    }

    @GetMapping("/api/post/byTag")
    public ResponseEntity<Posts> getPostsByTag(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "tag") String tag
    ) {
        return ResponseEntity.ok(postService.searchByTag(offset, limit, tag));
    }
    @Secured("ROLE_MODERATOR")
    @GetMapping("/api/post/moderation")
    public ResponseEntity<Posts> moderation(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status
    ) {
        return ResponseEntity.ok(postService.getPostsForModeration(offset, limit, status, authService.getAuthorizedUser(token)));
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/post/my")
    public ResponseEntity<Posts> my(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status) {
        return ResponseEntity.ok(postService.getMyPosts(offset, limit, status, authService.getAuthorizedUser(token)));
    }
    @Secured("ROLE_USER")
    @PostMapping("/api/post")
    public ResponseEntity<PostAddResponse> add(@CookieValue(value = "token", defaultValue = "invalid") String token, @RequestBody AddPostRequest request) throws ParseException {
        return ResponseEntity.ok(postService.add(request, authService.getAuthorizedUser(token)));
    }

    @Secured("ROLE_USER")
    @PutMapping("/api/post/{id}")
    public ResponseEntity<PostAddResponse> edit(@PathVariable int id, @RequestBody AddPostRequest request){
        return ResponseEntity.ok(postService.edit(id, request));
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/post/like")
    public ResponseEntity<HashMap<String, Boolean>> likeVote(@CookieValue(value = "token", defaultValue = "invalid") String token,
                                   @RequestBody PostIdRequest request){
        return ResponseEntity.ok(postService.like(request, authService.getAuthorizedUser(token)));
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/post/dislike")
    public ResponseEntity<HashMap<String, Boolean>> dislikeVote(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestBody PostIdRequest request){
        return ResponseEntity.ok(postService.dislike(request, authService.getAuthorizedUser(token)));
    }
}
