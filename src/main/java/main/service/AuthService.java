package main.service;

import main.model.User;
import main.model.request.UserRequest;
import main.model.response.ViewModelFactory;
import main.repository.UserRepository;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashMap;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private HashMap<String, Integer> sessions = new HashMap<>();
    private final static String RESULT_KEY_NAME = "result";
    private final static String USER_KEY_NAME = "user";
    private final static Logger logger = LogManager.getLogger();


    public HashMap<Object, Object> authenticate(UserRequest userDto) {
        HashMap<Object, Object> responseBody = new HashMap<>();
        responseBody.put(RESULT_KEY_NAME, false);
        String email = userDto.getEmail();
        String password = userDto.getPassword();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && password.equals(user.getPassword())) {//success
            sessions.put(RequestContextHolder.currentRequestAttributes().getSessionId(), user.getId());
            responseBody.put(RESULT_KEY_NAME, true);
            responseBody.put(USER_KEY_NAME, ViewModelFactory.getUserInfo(user));
        }
        logger.log(Level.INFO, "login user");
        return responseBody;
    }

    public HashMap<Object, Object> authCheck(String session) {
        HashMap<Object, Object> responseBody = new HashMap<>();
        responseBody.put(RESULT_KEY_NAME, false);
        if (sessions.containsKey(session)) {
            User user = userRepository.findById(sessions.get(session)).orElse(null);
            responseBody.put(RESULT_KEY_NAME, true);
            responseBody.put(USER_KEY_NAME, ViewModelFactory.getUserInfo(user));
        }
        logger.log(Level.INFO, "Auth check with session "+session);
        return responseBody;
    }

    public HashMap<String, Boolean> restorePassword(String email) {
        HashMap<String, Boolean> response = new HashMap<>();
        response.put(RESULT_KEY_NAME, false);
        User user = userRepository.findByEmail(email).orElse(null);
        ///
        return null;
    }

    public HashMap<Object, Object> register(){
        return null;
    }

    public HashMap<String, Boolean> logout(String session){
        HashMap<String, Boolean> response = new HashMap<>();
        response.put(RESULT_KEY_NAME, true);
        sessions.remove(session);
        logger.log(Level.INFO, "logout session "+ session);
        return response;
    }
}
