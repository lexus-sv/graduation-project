package main.controller;

import main.api.request.PostIdRequest;
import main.api.response.post.PostWithCommentsAndTags;
import main.api.response.post.Posts;
import main.model.*;
import main.api.request.AddPostRequest;
import main.api.response.PostModelType;
import main.api.response.UserModelType;
import main.api.response.ViewModelFactory;
import main.api.response.post.PostBehavior;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.service.AuthService;
import main.service.PostService;
import main.service.impl.PostServiceImpl;
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
    private PostService postService;

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

    @GetMapping("/api/post/moderation")
    public ResponseEntity<?> moderation(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status
    ) {
        Posts posts = postService.getPostsForModeration(offset, limit, status);
        return posts != null ? ResponseEntity.ok(posts) : new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/api/post/my")
    public ResponseEntity my(
            @RequestParam(value = "offset") int offset,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "status") String status) {
        Posts posts = postService.getMyPosts(offset, limit, status);
        return posts != null ? ResponseEntity.ok(posts) : new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/api/post")
    public ResponseEntity<?> post(@RequestBody AddPostRequest request) throws ParseException {
        return ResponseEntity.ok(postService.add(request));
    }

    @PutMapping("/api/post/{id}")
    public ResponseEntity<?> edit(@PathVariable int id, AddPostRequest request){
        return ResponseEntity.ok(postService.edit(id, request));
    }

    @PostMapping("/api/post/like")
    public ResponseEntity likeVote(@RequestBody PostIdRequest request){
        Object response = postService.like(request);
        return response != null
                ? ResponseEntity.ok(response)
                : new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/api/post/dislike")
    public ResponseEntity dislikeVote(@RequestBody PostIdRequest request){
        Object response = postService.dislike(request);
        return response != null
                ? ResponseEntity.ok(response)
                : new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
