package main.service.impl;

import lombok.extern.slf4j.Slf4j;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.api.auth.response.RegisterErrorResponse;
import main.api.auth.response.ResultResponse;
import main.api.auth.response.error.RegisterError;
import main.api.general.StatisticsResponse;
import main.api.general.profile.ProfileEditRequest;
import main.api.general.profile.ProfileEditResponse;
import main.api.general.profile.ProfileErrors;
import main.model.User;
import main.repository.UserRepository;
import main.service.CaptchaService;
import main.service.ImageService;
import main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
    private final ImageService imageService;

    private final String ERROR_EMAIL = "Email уже зарегистрирован";
    private final String ERROR_NAME = "Имя указано неверно.";
    private final String ERROR_PASSWORD = "Пароль не может быть меньше 6 символов";
    private final String ERROR_PHOTO = "Размер фото превышает 5 мб";
    private final String ERROR_CAPTCHA = "Код с картинки указан неверно";

    @Value("${files.maxFileUploadSize}")
    private int maxFileSizeInBytes;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, CaptchaService captchaService, ImageService imageService)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
        this.imageService = imageService;
    }

    @Override
    public AuthResponse register(RegisterUserRequest request)
    {
        RegisterError errors = new RegisterError();
        if (userRepository.existsByEmail(request.getEmail()))
        {
            errors.setEmail(ERROR_EMAIL);
        }
        if (request.getName().length() < 3 || !isValidName(request.getName()))
        {
            errors.setName(ERROR_NAME);
        }
        if (request.getPassword().length() < 6)
        {
            errors.setPassword(ERROR_PASSWORD);
        }
        if (!captchaService.isValidCaptcha(request.getCaptcha(), request.getCaptchaSecret()))
        {
            errors.setCaptcha(ERROR_CAPTCHA);
        }
        if (errors.hasAtLeastOneError())
        {
            log.info("IN register request has errors {}", errors);
            return new RegisterErrorResponse(errors);
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
    public List<User> getAll()
    {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String email)
    {
        return userRepository
                .findByEmail(email)
                .orElse(null);
    }

    @Override
    public User save(User user)
    {
        return userRepository.save(user);
    }

    @Override
    public ProfileEditResponse edit(ProfileEditRequest request, User user)
    {
        ProfileEditResponse response = new ProfileEditResponse();
        ProfileErrors errors = new ProfileErrors();

        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();
        Boolean removePhoto = request.getRemovePhoto();
        MultipartFile photo = null;
        try
        {
            photo = (MultipartFile) request.getPhoto();
        } catch (ClassCastException ignored)
        {
        }

        boolean isDataCorrect = true;

        if (email != null)
        {
            if (findByEmail(email) == null || email.equalsIgnoreCase(user.getEmail()))
            {
                user.setEmail(email);
            } else
            {
                errors.setEmail(ERROR_EMAIL);
                isDataCorrect = false;
            }
        }
        if (name != null)
        {
            if (name.length() > 3 && isValidName(name))
            {
                user.setName(name);
            } else
            {
                errors.setName(ERROR_NAME);
                isDataCorrect = false;
            }
        }
        if (password != null)
        {
            if (password.length() >= 6)
            {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            } else
            {
                errors.setPassword(ERROR_PASSWORD);
                isDataCorrect = false;
            }
        }
        if (removePhoto != null && removePhoto)
        {
            user.setPhoto(null);
        }
        if (photo != null)
        {
            if (photo.getSize() < maxFileSizeInBytes)
            {
                user.setPhoto(imageService.saveImage(photo, true));
            } else
            {
                errors.setPhoto(ERROR_PHOTO);
                isDataCorrect = false;
            }
        }
        if (isDataCorrect)
        {
            log.info("user :{} saved", userRepository.save(user));
        } else
        {
            response.setErrors(errors);
            log.info("IN edit request is incorrect, errors :{}", errors);
        }
        response.setResult(isDataCorrect);
        log.info("User edited successfully");
        return response;
    }

    @Override
    public StatisticsResponse getUserStatistics(User user)
    {
        log.info("IN getMyStatistics user {} got his stats", user);
        return userRepository.getStatisticsByUser(user);
    }

    @Override
    public User findById(int id)
    {
        return userRepository
                .findById(id)
                .orElse(null);
    }

    @Override
    public void delete(int id)
    {
        userRepository.deleteById(id);
    }

    private boolean isValidName(String name)
    {
        Pattern p = Pattern.compile("^[ a-zA-Zа-яА-Я0-9_.-]*$");
        Matcher matcher = p.matcher(name);
        return matcher.matches();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver
                = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(maxFileSizeInBytes);
        return multipartResolver;
    }
}
