package main.api.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultResponse implements AuthResponse {

  private boolean result;
}
