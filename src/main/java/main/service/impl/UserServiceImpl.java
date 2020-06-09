package main.service.impl;

import lombok.extern.slf4j.Slf4j;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.api.auth.response.RegisterErrorResponse;
import main.api.auth.response.ResultResponse;
import main.api.auth.response.error.RegisterError;
import main.model.User;
import main.repository.UserRepository;
import main.service.CaptchaService;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, CaptchaService captchaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
    }

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        RegisterError registerError = new RegisterError();
        if (userRepository.existsByEmail(request.getEmail())) {
            registerError.setEmail("Email уже зарегистрирован");
        }
        if (request.getName().length() < 3 || !isValidName(request.getName())) {
            registerError.setName("Имя указано неверно.");
        }
        if (request.getPassword().length() < 6) {
            registerError.setPassword("Пароль не может быть меньше 6 символов");
        }
        if (!captchaService.isValidCaptcha(request.getCaptcha(), request.getCaptchaSecret())) {
            registerError.setCaptcha("Код с картинки введён неверно");
        }
        if (registerError.hasAtLeastOneError()) {
            log.info("IN register request has errors {}", registerError);
            return new RegisterErrorResponse(registerError);
        }
        String password = request.getPassword();
        User user = new User();
        user.setModerator(false);
        user.setRegistrationDate(new Date());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setCode(null);
        user.setPhoto(null);

        log.info("IN register user {} has been successfully registered", userRepository.save(user));
        return new ResultResponse(true);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(int id) {
        userRepository.deleteById(id);
    }

    private boolean isValidName(String name) {
        Pattern p = Pattern.compile("^[ a-zA-Zа-яА-Я0-9_.-]*$");
        Matcher matcher = p.matcher(name);
        return matcher.matches();
    }
}
