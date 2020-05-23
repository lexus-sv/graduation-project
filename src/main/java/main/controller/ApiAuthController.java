package main.controller;

import main.model.User;
import main.model.request.UserRequest;
import main.model.response.ViewModelFactory;
import main.security.jwt.JwtTokenProvider;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth/")
public class ApiAuthController {

    private HashMap<String, Integer> sessions = new HashMap<>();

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    @Autowired
    public ApiAuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody UserRequest userDto){
        try {
            String email = userDto.getEmail();
            UsernamePasswordAuthenticationToken t = new UsernamePasswordAuthenticationToken(email, userDto.getPassword());
            authenticationManager.authenticate(t);
            User user = userService.findByEmail(email);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(t);
            if (user == null) {
                throw new UsernameNotFoundException("User with email: " + email + " not found");
            }

            String token = jwtTokenProvider.createToken(email);
            System.out.println(token);
            sessions.put(token, user.getId());
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
            Map<Object, Object> response = new HashMap<>();
            response.put("result", true);
            response.put("user", ViewModelFactory.getUserInfo(user));

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

//    @PostMapping(value = "/api/auth/login")
//    public ResponseEntity login(@RequestBody UserRequest userDto) {
//        String email = userDto.getEmail();
//        String password = userDto.getPassword();
//        Optional<User> user = userRepository.findByEmail(email);
//        JSONObject responseBody = new JSONObject();
//        if (user.isPresent()) {
//            if (user.get().getPassword().equals(password)) {
//                sessions.put(RequestContextHolder.currentRequestAttributes().getSessionId(), user.get().getId());
//                responseBody.put("result", true);
//                responseBody.put("user", ViewModelFactory.getUserInfo(user.get()));
//                return new ResponseEntity(responseBody, HttpStatus.OK);
//            } else return new ResponseEntity(responseBody.put("result", false), HttpStatus.NOT_FOUND);
//        } else return new ResponseEntity(responseBody.put("result", false), HttpStatus.NOT_FOUND);
//    }

    @GetMapping("check")
    public ResponseEntity authCheck() {

        Map<Object, Object> responseBody = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        if(authentication instanceof AnonymousAuthenticationToken){
            responseBody.put("result", false);
        } else responseBody.put("result", true);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }
}
