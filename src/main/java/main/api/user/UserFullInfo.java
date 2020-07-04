package main.api.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFullInfo {

  private Integer id;

  private String name;

  private String photo;

  private String email;

  private Boolean moderation;

  private Integer moderationCount;

  private Boolean settings;

}
