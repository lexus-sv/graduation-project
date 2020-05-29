package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostIdRequest {
    @JsonProperty("post_id")
    private int postId;
}
