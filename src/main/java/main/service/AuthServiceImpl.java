package main.service;

import lombok.extern.slf4j.Slf4j;
import main.api.auth.request.PasswordUserRequest;
import main.api.auth.response.*;
import main.api.auth.response.error.PassError;
import main.model.User;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.request.LoginUserRequest;
import main.api.ViewModelFactory;
import main.repository.PostRepository;
import main.security.jwt.JwtAuthenticationException;
import main.security.jwt.JwtTokenProvider;
import main.service.impl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private EmailServiceImpl emailService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;

    private final static String PASSWORD_RESTORE_PATH = "/login/change-password?token=";

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder passwordEncoder, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
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

    @Override
    public AuthResponse login(LoginUserRequest request, HttpServletResponse response) {
        try {
            String email = request.getEmail();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
            User user = userService.findByEmail(email);

            if (user == null) {
                throw new UsernameNotFoundException("User with username: " + email + " not found");
            }
            String token = jwtTokenProvider.createToken(email);
            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge((int) jwtTokenProvider.getCookieMaxAge());
            response.addCookie(cookie);
            log.info("IN login user {} has logged in", user);
            return new LoginUserResponse(true, ViewModelFactory.getFullInfoUser(user));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        AuthResponse result = userService.register(request);
        if (result instanceof ResultResponse) {//Сообщения подкорректировать
            emailService.sendSimpleMessage(request.getEmail(), "Devpub registration", "Рады приветствовать Вас на нашем ресурсе!");
        }
        return result;
    }

    @Override
    public AuthResponse logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.info("IN logout has been successfully made");
        return new ResultResponse(true);
    }

    @Override
    public AuthResponse authCheck(String token) {
        try {
            return new AuthCheckResponse(true, ViewModelFactory
                    .getFullInfoUser(userService.findByEmail(jwtTokenProvider.getUsername(token))));
        } catch (Exception e) {
            log.info("IN authCheck exception {} caught", e.getClass());
            return new ResultResponse(false);
        }
    }

    @Override
    public User getAuthorizedUser(String token) {
        return userService.findByEmail(jwtTokenProvider.getUsername(token));
    }

    @Override
    public boolean isAuthorized(String token) {
        return false;
    }

    @Override
    public AuthResponse passwordRecovery(String email, String url) {
        User user = userService.findByEmail(email);
        if (user != null) {
            user.setCode(captchaService.generateRandomString(50));
            userService.save(user);
            String token = jwtTokenProvider.createToken(user.getEmail() + ":" + user.getCode());
            emailService.sendPasswordRecovery(email, url + PASSWORD_RESTORE_PATH + token);
            log.info("IN passwordRecovery recovery code successfully sent to user with email {}", email);
            return new ResultResponse(true);
        } else {
            log.info("IN passwordRecovery user with email {} not found", email);
            return new ResultResponse(false);
        }
    }

    @Override
    public AuthResponse passwordSet(PasswordUserRequest dto, String referer) {
        PassError errors = new PassError();
        try {
            URL ub = new URL(referer);
            dto.setCode(ub.getQuery());
            String token = dto.getCode().replaceAll("token=", "");
            String[] strings = jwtTokenProvider.getUsername(token).split(":");
            if (strings.length == 2) {
                if (captchaService.isValidCaptcha(dto.getCaptcha(), dto.getCaptchaSecret())) {
                    User person = userService.findByEmail(strings[0]);
                    if (person.getCode().equals(strings[1])) {
                        person.setPassword(passwordEncoder.encode(dto.getPassword()));
                        person.setCode(dto.getCaptchaSecret());
                        userService.save(person);
                        log.info("IN setPassword password of user {} changed successfully", person.getEmail());
                        return new ResultResponse(true);
                    } else {
                        errors.setCode("Ссылка для восстановления пароля устарела.<a href=\"restore-password\">Запросить ссылку снова</a>");
                    }

                } else {
                    log.info("IN setPassword was entered wrong captcha");
                    errors.setCaptcha("Код с картинки введён неверно");
                }
            }
            log.info("IN setPassword Wrong format of request {}", Arrays.toString(strings));
            return new PasswordErrorResponse(errors);

        } catch (MalformedURLException e) {
            log.info("IN setPassword malformed exception {}", e.getMessage());
            return new PasswordErrorResponse(errors);
        } catch (JwtAuthenticationException e) {
            log.info("IN passwordSet token found invalid");
            errors.setCode("Ссылка для восстановления пароля устарела.<a href=\"restore-password\">Запросить ссылку снова</a>");
            return new PasswordErrorResponse(errors);
        }
    }
}