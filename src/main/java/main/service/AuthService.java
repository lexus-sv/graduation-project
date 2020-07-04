package main.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import main.api.auth.request.LoginUserRequest;
import main.api.auth.request.PasswordUserRequest;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.request.RestorePasswordRequest;
import main.api.auth.response.AuthResponse;
import main.model.User;

public interface AuthService {

  AuthResponse login(LoginUserRequest request, HttpServletResponse response);

  AuthResponse register(RegisterUserRequest request);

  AuthResponse logout(HttpServletResponse response);

  AuthResponse authCheck(String token);

  User getAuthorizedUser(String token);

  boolean isAuthorized(String token);

  AuthResponse passwordRecovery(RestorePasswordRequest dto, HttpServletRequest request);

  AuthResponse passwordSet(PasswordUserRequest dto, String referer);
}
