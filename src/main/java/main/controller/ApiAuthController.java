package main.controller;

import main.api.auth.request.PasswordUserRequest;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.request.RestorePasswordRequest;
import main.api.auth.response.AuthResponse;
import main.service.AuthServiceImpl;
import main.api.auth.request.LoginUserRequest;
import main.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/auth/")
public class ApiAuthController {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping(value = "login")
    public ResponseEntity<?> login(@RequestBody LoginUserRequest userDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(userDto, response));
    }

    @GetMapping(value = "check")
    public ResponseEntity<?> authCheck(@CookieValue(value = "token", defaultValue = "invalid") String token) {
        return ResponseEntity.ok(authService.authCheck(token));
    }

    @GetMapping(value = "logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        return ResponseEntity.ok(authService.logout(response));
    }

    @GetMapping(value = "captcha")
    public ResponseEntity<?> captcha() throws IOException {
        return ResponseEntity.ok(captchaService.createCaptcha());
    }

    @PostMapping(value = "register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest userDto){
        return ResponseEntity.ok(authService.register(userDto));
    }

    @PostMapping(value = "restore")
    public ResponseEntity<?> restorePassword(HttpServletRequest request, @RequestBody RestorePasswordRequest dto){
        AuthResponse responseDto = authService.passwordRecovery(dto.getEmail(),
                request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "login/change-password")
    public void sendToken(@RequestParam(name = "token") String token){
        System.out.println(token);
    }

    @PostMapping(value = "password")
    public ResponseEntity<?> changePass(@RequestHeader(name = "Referer") String referer,
                                        @RequestBody PasswordUserRequest dto){
        return ResponseEntity.status(HttpStatus.OK).body(authService.passwordSet(dto, referer));
    }
}
