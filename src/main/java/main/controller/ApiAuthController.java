package main.controller;

import main.api.request.RegisterUserRequest;
import main.service.AuthService;
import main.api.request.UserRequest;
import main.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/auth/")
public class ApiAuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping(value = "login")
    public ResponseEntity login(@RequestBody UserRequest userDto) {
        HashMap<Object, Object> response = authService.authenticate(userDto);
        return new ResponseEntity(response,  (boolean) response.get("result") ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "check")
    public ResponseEntity authCheck() {
        HashMap<Object, Object> response = authService.authCheck(RequestContextHolder.currentRequestAttributes().getSessionId());
        return new ResponseEntity(response,  HttpStatus.OK);
    }

    @GetMapping(value = "logout")
    public ResponseEntity logout(){
        return new ResponseEntity(authService.logout(RequestContextHolder.currentRequestAttributes().getSessionId()), HttpStatus.OK);
    }

    @GetMapping(value = "captcha")
    public ResponseEntity<?> captcha() throws IOException {
        return new ResponseEntity(captchaService.createCaptcha(), HttpStatus.OK);
    }

    @PostMapping(value = "register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest userDto){
        return new ResponseEntity(authService.register(userDto), HttpStatus.OK);
    }
}
