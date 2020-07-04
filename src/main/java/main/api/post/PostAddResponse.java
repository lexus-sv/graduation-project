package main.api.post;

import lombok.Data;

@Data
public class PostAddResponse {

  private boolean result;
  private PostResponseErrors errors;
}
