package main.service;

import main.api.post.AddPostRequest;
import main.api.post.PostIdRequest;
import main.api.post.response.PostWithCommentsAndTags;
import main.api.post.response.Posts;
import main.model.User;

import java.util.HashMap;

public interface PostService {

    Posts getAll(int offset, int limit, String mode);

    Posts search(int offset, int limit, String query);

    PostWithCommentsAndTags findPostById(int id);

    Posts searchByDate(int offset, int limit, String date);

    Posts searchByTag(int offset, int limit, String tagName);

    Posts getPostsForModeration(int offset, int limit, String status, User user);

    Posts getMyPosts(int offset, int limit, String status, User user);

    HashMap<Object, Object> add(AddPostRequest request, User user);

    HashMap<Object, Object> edit(int id, AddPostRequest request);

    HashMap<String, Boolean> like(PostIdRequest request, User user);

    HashMap<String, Boolean> dislike(PostIdRequest request, User user);

}
