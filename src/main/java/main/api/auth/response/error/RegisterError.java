package main.api.auth.response.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterError {
    private String email;
    private String name;
    private String password;
    private String captcha;

    public boolean hasAtLeastOneError(){
        return email!=null || name!=null || password != null || captcha !=null;
    }
}
