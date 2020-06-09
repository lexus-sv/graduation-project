package main.api.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginUserRequest {
    @JsonProperty("e_mail")
    private String email;

    private String password;

    public LoginUserRequest() {
    }

    public LoginUserRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
