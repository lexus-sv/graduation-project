package main.controller;

import main.model.User;
import main.model.request.UserRequest;
import main.model.response.ViewModelFactory;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Optional;

@RestController
public class ApiAuthController {

    private HashMap<String, Integer> sessions = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/api/auth/login")
    public ResponseEntity login(@RequestBody UserRequest userDto) {
        String email = userDto.getEmail();
        String password = userDto.getPassword();
        System.out.println("email: "+email + " password: "+password);
        Optional<User> user = userRepository.findByEmail(email);
        JSONObject responseBody = new JSONObject();
        if (user.isPresent()) {
            if (user.get().getPassword().equals(password)) {
                sessions.put(RequestContextHolder.currentRequestAttributes().getSessionId(), user.get().getId());
                responseBody.put("result", true);
                responseBody.put("user", ViewModelFactory.getUserInfo(user.get()));
                return new ResponseEntity(responseBody, HttpStatus.OK);
            } else return new ResponseEntity(responseBody.put("result", false), HttpStatus.NOT_FOUND);
        } else return new ResponseEntity(responseBody.put("result", false), HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/api/auth/check")
    public ResponseEntity authCheck() {
        JSONObject responseBody = new JSONObject();
        String session = RequestContextHolder.currentRequestAttributes().getSessionId();
        if(sessions.containsKey(session)){
            User user = userRepository.findById(sessions.get(session)).get();
            responseBody.put("result",true);
            responseBody.put("user", ViewModelFactory.getUserInfo(user));
        } else responseBody.put("result", false);

        return new ResponseEntity(responseBody, HttpStatus.OK);
    }
}
