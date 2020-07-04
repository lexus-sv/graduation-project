package main.service;

import java.util.HashMap;
import main.api.post.AddPostRequest;
import main.api.post.PostAddResponse;
import main.api.post.PostIdRequest;
import main.api.post.response.Post;
import main.api.post.response.Posts;
import main.model.User;

public interface PostService {

  Posts getAll(int offset, int limit, String mode);

  Posts search(int offset, int limit, String query);

  Post findPostById(int id);

  Posts searchByDate(int offset, int limit, String date);

  Posts searchByTag(int offset, int limit, String tagName);

  Posts getPostsForModeration(int offset, int limit, String status, User user);

  Posts getMyPosts(int offset, int limit, String status, User user);

  PostAddResponse add(AddPostRequest request, User user);

  PostAddResponse edit(int id, AddPostRequest request);

  HashMap<String, Boolean> like(PostIdRequest request, User user);

  HashMap<String, Boolean> dislike(PostIdRequest request, User user);

}
