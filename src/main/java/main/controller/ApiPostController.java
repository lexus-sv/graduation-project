package main.controller;

import main.api.post.AddPostRequest;
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
    public ResponseEntity<?> getPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "mode") String mode) {
        return new ResponseEntity(postService.getAll(offset, limit, mode), HttpStatus.OK);
    }

    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
    public ResponseEntity searchPosts(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "query") String query) {
        return new ResponseEntity(postService.search(offset, limit, query), HttpStatus.OK);
    }


    @GetMapping(value = "/api/post/{id}")
    public ResponseEntity getPostById(@PathVariable int id) {
        PostWithCommentsAndTags post = postService.findPostById(id);
        return post != null ? ResponseEntity.ok(post) : new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/api/post/byDate")
    public ResponseEntity<?> getPostsByDate(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "date") String date
    ) {
        Posts posts = postService.searchByDate(offset, limit, date);
        return posts != null ? ResponseEntity.ok(posts) : new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/api/post/byTag")
    public ResponseEntity<?> getPostsByTag(
            @RequestParam(name = "offset") int offset,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "tag") String tag
    ) {
        return ResponseEntity.ok(postService.searchByTag(offset, limit, tag));
    }
    @Secured("ROLE_MODERATOR")
    @GetMapping("/api/post/moderation")
    public ResponseEntity<?> moderation(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status
    ) {
        Posts posts = postService.getPostsForModeration(offset, limit, status, authService.getAuthorizedUser(token));
        return posts != null ? ResponseEntity.ok(posts) : new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/post/my")
    public ResponseEntity my(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status) {
        Posts posts = postService.getMyPosts(offset, limit, status, authService.getAuthorizedUser(token));
        return posts != null ? ResponseEntity.ok(posts) : new ResponseEntity(HttpStatus.FORBIDDEN);
    }
    @Secured("ROLE_USER")
    @PostMapping("/api/post")
    public ResponseEntity<?> add(@CookieValue(value = "token", defaultValue = "invalid") String token, @RequestBody AddPostRequest request) throws ParseException {
        return ResponseEntity.ok(postService.add(request, authService.getAuthorizedUser(token)));
    }

    @Secured("ROLE_USER")
    @PutMapping("/api/post/{id}")
    public ResponseEntity<?> edit(@PathVariable int id, @RequestBody AddPostRequest request){
        return ResponseEntity.ok(postService.edit(id, request));
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/post/like")
    public ResponseEntity likeVote(@CookieValue(value = "token", defaultValue = "invalid") String token,
                                   @RequestBody PostIdRequest request){
        Object response = postService.like(request, authService.getAuthorizedUser(token));
        return response != null
                ? ResponseEntity.ok(response)
                : new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/post/dislike")
    public ResponseEntity dislikeVote(
            @CookieValue(value = "token", defaultValue = "invalid") String token,
            @RequestBody PostIdRequest request){
        Object response = postService.dislike(request, authService.getAuthorizedUser(token));
        return response != null
                ? ResponseEntity.ok(response)
                : new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
