package main.api.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.api.auth.response.error.PassError;

@Data
public class PasswordErrorResponse implements AuthResponse {
    private final boolean result=false;
    private PassError errors;

    public PasswordErrorResponse(PassError errors){
        this.errors = errors;
    }
}
