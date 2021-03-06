package main.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import main.api.auth.request.LoginUserRequest;
import main.api.auth.request.PasswordUserRequest;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.request.RestorePasswordRequest;
import main.api.auth.response.AuthResponse;
import main.service.CaptchaService;
import main.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth/")
public class ApiAuthController {

  private final AuthServiceImpl authService;

  private final CaptchaService captchaService;

  @Autowired
  public ApiAuthController(AuthServiceImpl authService, CaptchaService captchaService) {
    this.authService = authService;
    this.captchaService = captchaService;
  }

  @PostMapping(value = "login")
  public ResponseEntity<?> login(@RequestBody LoginUserRequest userDto,
      HttpServletResponse response) {
    return ResponseEntity.ok(authService.login(userDto, response));
  }

  @GetMapping(value = "check")
  public ResponseEntity<?> authCheck(
      @CookieValue(value = "token", defaultValue = "invalid") String token) {
    return ResponseEntity.ok(authService.authCheck(token));
  }

  @GetMapping(value = "logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    return ResponseEntity.ok(authService.logout(response));
  }

  @GetMapping(value = "captcha")
  public ResponseEntity<?> captcha() throws IOException {
    return ResponseEntity.ok(captchaService.createCaptcha());
  }

  @PostMapping(value = "register")
  public ResponseEntity<?> register(@RequestBody RegisterUserRequest userDto) {
    return ResponseEntity.ok(authService.register(userDto));
  }

  @PostMapping(value = "restore")
  public ResponseEntity<?> restorePassword(HttpServletRequest request,
      @RequestBody RestorePasswordRequest dto) {
    AuthResponse responseDto = authService.passwordRecovery(dto, request);
    return ResponseEntity.ok(responseDto);
  }

  @PostMapping(value = "password")
  public ResponseEntity<?> changePass(@RequestHeader(name = "Referer") String referer,
      @RequestBody PasswordUserRequest dto) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.passwordSet(dto, referer));
  }
}
