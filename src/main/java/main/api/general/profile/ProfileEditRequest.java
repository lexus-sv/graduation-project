package main.api.general.profile;

import lombok.Data;

@Data
public class ProfileEditRequest {

  private Object photo;
  private Boolean removePhoto;
  private String name;
  private String email;
  private String password;
}
