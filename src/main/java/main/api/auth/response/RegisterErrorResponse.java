package main.api.auth.response;

import lombok.Data;
import main.api.auth.response.error.RegisterError;

@Data
public class RegisterErrorResponse implements AuthResponse {
    private final boolean result = false;
    private RegisterError errors;

    public RegisterErrorResponse(RegisterError errors){
        this.errors = errors;
    }
}
