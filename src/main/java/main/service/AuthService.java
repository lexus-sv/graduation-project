package main.service;

import main.model.ModerationStatus;
import main.model.User;
import main.api.request.RegisterUserRequest;
import main.api.request.UserRequest;
import main.api.response.ViewModelFactory;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.impl.EmailServiceImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private EmailServiceImpl emailService;

    private HashMap<String, Integer> sessions = new HashMap<>();
    private final static String RESULT_KEY_NAME = "result";
    private final static String USER_KEY_NAME = "user";
    private final static String ERROR_KEY_NAME = "errors";
    private final static String PASSWORD_RESTORE_PATH = "/login/change-password/";
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
            responseBody.put(USER_KEY_NAME, ViewModelFactory.getFullInfoUser(user, postRepository.countByModerationStatusNot(ModerationStatus.ACCEPTED)));
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
            responseBody.put(USER_KEY_NAME, ViewModelFactory.getFullInfoUser(user, postRepository.countByModerationStatusNot(ModerationStatus.ACCEPTED)));
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

    public HashMap<Object, Object> register(RegisterUserRequest request){
        HashMap<Object, Object> response = new HashMap<>();
        response.put(RESULT_KEY_NAME, false);
        JSONObject errors = new JSONObject();
        response.put(ERROR_KEY_NAME, errors);
        String email = request.getEmail();
        if(userRepository.existsByEmail(email)){
            errors.put("email", "Email already exists");
        }
        if(request.getName().length()<3 || !isValidName(request.getName())){
            errors.put("name", "Имя указано неверно.");
        }
        if(request.getPassword().length()<6){
            errors.put("password", "Пароль не может быть меньше 6 символов");
        }
        if(!captchaService.isValidCaptcha(request.getCaptcha(), request.getCaptchaSecret())){
            errors.put("captcha", "Код с картинки введён неверно");
        }
        if(errors.isEmpty()){
            response.put(RESULT_KEY_NAME, true);
            response.remove(ERROR_KEY_NAME);
            String password = request.getPassword();
            User user = new User();
            user.setModerator(false);
            user.setRegistrationDate(new Date());
            user.setName(request.getName());
            user.setEmail(email);
            user.setPassword(password);
            user.setCode(null);
            user.setPhoto(null);

            userRepository.save(user);
            logger.log(Level.INFO, "User "+user+" successfully registered");
            return response;
        }
        return response;
    }

    public HashMap<String, Boolean> logout(String session){
        HashMap<String, Boolean> response = new HashMap<>();
        response.put(RESULT_KEY_NAME, true);
        sessions.remove(session);
        logger.log(Level.INFO, "logout session "+ session);
        return response;
    }

    private boolean isValidName(String name){
        Pattern p = Pattern.compile("^[ a-zA-Z0-9_.-]*$");
        Matcher matcher = p.matcher(name);
        return matcher.matches();
    }

    public User getCurrentUser(String session){
        return isAuthorized(session) ? userRepository.findById(sessions.get(session)).orElse(null)
                : null;
    }

    public boolean isAuthorized(String session){
        return sessions.containsKey(session);
    }

    public HashMap<String, Boolean> sendEmail(String email){
        HashMap<String, Boolean> response = new HashMap<>();
        response.put(RESULT_KEY_NAME, false);

        User user = userRepository.findByEmail(email).orElse(null);
        if(user==null){
            return response;
        }
        response.put(RESULT_KEY_NAME, true);

        String hash = captchaService.generateRandomString(50);
        user.setCode(hash);
        userRepository.save(user);
        String restoreLink = PASSWORD_RESTORE_PATH + hash;
        String subject = "PASSWORD RESTORATION";

        emailService.sendSimpleMessage(email, subject, restoreLink);
        return response;
    }
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("devpubemailservice@gmail.com");
        mailSender.setPassword("jkwgjhaxnzqhmmqi");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
