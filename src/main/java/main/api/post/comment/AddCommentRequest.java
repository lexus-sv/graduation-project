package main.api.post.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddCommentRequest {
    @JsonProperty("parent_id")
    private Integer parentId;
    @JsonProperty("post_id")
    private Integer postId;
    private String text;
}
