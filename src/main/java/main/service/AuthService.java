package main.service;

import main.api.auth.request.LoginUserRequest;
import main.api.auth.request.PasswordUserRequest;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.model.User;

import javax.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(LoginUserRequest request, HttpServletResponse response);
    AuthResponse register(RegisterUserRequest request);
    AuthResponse logout(HttpServletResponse response);
    AuthResponse authCheck(String token);
    User getAuthorizedUser(String token);
    boolean isAuthorized(String token);
    AuthResponse passwordRecovery(String email, String url);
    AuthResponse passwordSet(PasswordUserRequest dto, String referer);
}
