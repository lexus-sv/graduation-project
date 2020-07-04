package main.api.auth.response.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassError {

  private String code;
  private String password;
  private String captcha;
}
