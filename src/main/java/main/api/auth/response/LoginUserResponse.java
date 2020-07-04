package main.api.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.user.UserFullInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserResponse implements AuthResponse {

  private boolean result;
  private UserFullInfo user;
}
