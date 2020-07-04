package main.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import main.api.ViewModelFactory;
import main.api.auth.request.LoginUserRequest;
import main.api.auth.request.PasswordUserRequest;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.request.RestorePasswordRequest;
import main.api.auth.response.AuthCheckResponse;
import main.api.auth.response.AuthResponse;
import main.api.auth.response.LoginUserResponse;
import main.api.auth.response.PasswordErrorResponse;
import main.api.auth.response.RegisterErrorResponse;
import main.api.auth.response.ResultResponse;
import main.api.auth.response.error.PassError;
import main.api.auth.response.error.RegisterError;
import main.model.ModerationStatus;
import main.model.User;
import main.repository.PostRepository;
import main.security.jwt.JwtAuthenticationException;
import main.security.jwt.JwtTokenProvider;
import main.service.AuthService;
import main.service.CaptchaService;
import main.service.CookieManager;
import main.service.Settings;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final CaptchaService captchaService;

  private final EmailServiceImpl emailService;

  private final PostRepository postRepository;

  private final CookieManager cookieManager;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final BCryptPasswordEncoder passwordEncoder;
  private final UserService userService;
  private final Settings settings;

  private final static String PASSWORD_RESTORE_PATH = "/login/change-password?token=";
  private final static String MULTIUSER_MODE_KEY = "MULTIUSER_MODE";

  @Autowired
  public AuthServiceImpl(CaptchaService captchaService,
      EmailServiceImpl emailService, PostRepository postRepository,
      AuthenticationManager authenticationManager,
      JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder passwordEncoder,
      UserService userService, Settings settings) {
    this.captchaService = captchaService;
    this.emailService = emailService;
    this.postRepository = postRepository;
    this.settings = settings;
    this.cookieManager = CookieManager.getInstance();
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.passwordEncoder = passwordEncoder;
    this.userService = userService;
  }


  @Override
  public AuthResponse login(LoginUserRequest request, HttpServletResponse response) {
    try {
      String email = request.getEmail();
      authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
      User user = userService.findByEmail(email);
      if (user == null) {
        throw new UsernameNotFoundException("User with username: " + email + " not found");
      }
      String token = jwtTokenProvider.createToken(email);
      cookieManager
          .addCookie(response, token, (int) (jwtTokenProvider.getCookieMaxAge() - 1) / 1000);
      //to seconds, -1 for evading jwt expiration exception because of cookie with token expiring faster then jwt
      log.info("IN login user {} has logged in", user);
      return new LoginUserResponse(true, ViewModelFactory.getFullInfoUser(user,
          postRepository.countByModerationStatusAndActiveTrue(ModerationStatus.NEW)));
    } catch (AuthenticationException e) {
      return new ResultResponse(false);
    }
  }

  @Override
  public AuthResponse register(RegisterUserRequest request) {
    if (settings.getSetting(MULTIUSER_MODE_KEY)) {
      AuthResponse result = userService.register(request);
      if (result instanceof ResultResponse) {
        emailService.sendSimpleMessage(request
            .getEmail(), "Devpub registration", "Рады приветствовать Вас на нашем ресурсе!");
      }
      return result;
    } else {
      return new RegisterErrorResponse(new RegisterError());
    }
  }

  @SneakyThrows
  @Override
  public AuthResponse logout(HttpServletResponse response) {
    cookieManager.deleteCookie(response);
    log.info("IN logout has been successfully made");
    return new ResultResponse(true);
  }

  @Override
  public AuthResponse authCheck(String token) {
    try {
      return new AuthCheckResponse(true, ViewModelFactory
          .getFullInfoUser(userService.findByEmail(jwtTokenProvider.getUsername(token)),
              postRepository
                  .countByModerationStatusAndActiveTrue(ModerationStatus.NEW)));
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
  public AuthResponse passwordRecovery(RestorePasswordRequest dto, HttpServletRequest request) {
    String email = dto.getEmail();
    String url =
        request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
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
            errors.setCode(
                "Ссылка для восстановления пароля устарела.<a href=\"restore-password\">Запросить ссылку снова</a>");
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
      errors.setCode(
          "Ссылка для восстановления пароля устарела.<a href=\"restore-password\">Запросить ссылку снова</a>");
      return new PasswordErrorResponse(errors);
    }
  }
}
