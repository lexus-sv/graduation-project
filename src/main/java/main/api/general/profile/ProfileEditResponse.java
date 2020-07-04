package main.api.general.profile;

import lombok.Data;

@Data
public class ProfileEditResponse {

  private boolean result;
  private ProfileErrors errors;
}
