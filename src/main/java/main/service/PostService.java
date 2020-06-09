package main.service;

import main.api.post.AddPostRequest;
import main.api.post.PostIdRequest;
import main.api.post.response.PostWithCommentsAndTags;
import main.api.post.response.Posts;

import java.util.HashMap;

public interface PostService {

    Posts getAll(int offset, int limit, String mode);

    Posts search(int offset, int limit, String query);

    PostWithCommentsAndTags findPostById(int id);

    Posts searchByDate(int offset, int limit, String date);

    Posts searchByTag(int offset, int limit, String tagName);

    Posts getPostsForModeration(int offset, int limit, String status);

    Posts getMyPosts(int offset, int limit, String status);

    HashMap<Object, Object> add(AddPostRequest request);

    HashMap<Object, Object> edit(int id, AddPostRequest request);

    HashMap<String, Boolean> like(PostIdRequest request);

    HashMap<String, Boolean> dislike(PostIdRequest request);

}
