package main.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import main.InitInfo;
import main.api.*;
import main.api.post.comment.AddCommentRequest;
import main.api.general.ModerationRequest;
import main.api.general.calendar.CalendarResponse;
import main.api.post.tag.Tags;
import main.model.*;
import main.repository.*;
import main.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class GeneralServiceImpl implements GeneralService {

    @Value("${init.title}")
    private String title;
    @Value("${init.subtitle}")
    private String subtitle;
    @Value("${init.phone}")
    private String phone;
    @Value("${init.email}")
    private String email;
    @Value("${init.copyright}")
    private String copyright;
    @Value("${init.copyrightFrom}")
    private String copyrightFrom;

    private final PostRepository postRepository;

    private final PostCommentRepository commentRepository;

    private final TagRepository tagRepository;

    private final ImageService imageService;

    private final UserService userService;

    private final Settings settings;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final String STATISTICS_IS_PUBLIC_KEY = "STATISTICS_IS_PUBLIC";

    @Autowired
    public GeneralServiceImpl(AuthServiceImpl authService, PostRepository postRepository, PostCommentRepository commentRepository, TagRepository tagRepository, ImageService imageService, UserService userService, Settings settings)
    {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
        this.settings = settings;
        this.imageService = new ImageService();
    }

    @Override
    public InitInfo init()
    {
        return new InitInfo(title, subtitle, phone, email, copyright, copyrightFrom);
    }

    @Override
    public HashMap<String, Boolean> getSettings()
    {
        return settings.getSettings();
    }

    @Override
    public Tags getTags(String query)
    {
        List<Tag> tagList = query.length() != 0
                ? tagRepository.getRelevantTags(query)
                : tagRepository.findAll();
        return ViewModelFactory.getTags(tagList);
    }

    @Override
    public String uploadImage(MultipartFile image)
    {
        return imageService.saveImage(image, false);
    }

    @SneakyThrows
    @Override
    public byte[] getImageFromStorage(String path)
    {
        return imageService.getImageFromStorage(path);
    }

    @Override
    public HashMap<Object, Object> addComment(AddCommentRequest request, User user)
    {
        Post post = postRepository.findById(request.getPostId()).orElse(null);
        HashMap<Object, Object> response = new HashMap<>();
        if (request.getParentId() != null)
        {
            PostComment parentComment = commentRepository.findById(request.getParentId()).orElse(null);

            if (parentComment == null || post == null)
            {
                throw new NullPointerException("post or parentComment cant be null");
            }

            if (request.getText().length() < 5)
            {
                response.put("result", false);
                HashMap<String, String> errors = new HashMap<>();
                errors.put("text", "Текст комментария должен быть не меньше 5 символов");
                response.put("errors", errors);
                return response;
            }

            PostComment comment = new PostComment();
            comment.setParent(parentComment);
            comment.setPost(post);
            comment.setText(request.getText());
            comment.setUser(user);
            comment.setTime(new Date());
            response.put("id", commentRepository.save(comment).getId());
        } else
        {//if parentId == null
            if (request.getText().length() < 5)
            {
                response.put("result", false);
                HashMap<String, String> errors = new HashMap<>();
                errors.put("text", "Текст комментария должен быть не меньше 5 символов");
                response.put("errors", errors);
                return response;
            }
            PostComment comment = new PostComment();
            comment.setParent(null);
            comment.setPost(post);
            comment.setText(request.getText());
            comment.setUser(user);
            comment.setTime(new Date());
            commentRepository.save(comment);
            response.put("id", commentRepository.save(comment).getId());
        }
        return response;
    }

    @Override
    public void moderate(ModerationRequest request, User user)
    {
        Post post = postRepository.findById(request.getPostId()).get();
        post.setModerationStatus(ModerationStatus.getEqualStatus(request.getDecision()));
        post.setModerator(user);
        postRepository.save(post);
    }

    @Override
    public CalendarResponse getCalendar(Integer year)
    {
        if (year == null)
        {
            year = new Date().getYear();
        }
        HashMap<String, Long> posts = new HashMap<>();
        postRepository.getCalendarQuery(year).forEach(o -> posts.put(dateFormat.format(o.getDate()), o.getCount()));
        return new CalendarResponse(
                postRepository.getYears(),
                posts
        );
    }

    @Override
    public ProfileEditResponse editProfile(ProfileEditRequest request, User user)
    {
        return userService.edit(request, user);
    }

    @Override
    public MyStatisticsResponse getMyStatistics(User user)
    {
        return userService.getUserStatistics(user);
    }

    @Override
    public MyStatisticsResponse getAllStatistics()
    {
        if (settings.getSetting(STATISTICS_IS_PUBLIC_KEY))
        {
            log.info("getAllStatistics successfully");
            return postRepository.getGlobalStats();
        } else
        {
            return null;
        }
    }

    @Override
    public void editSettings(HashMap<String, Boolean> request)
    {
        settings.update(request);
        log.info("IN editSettings settings : {} have been applied", request);
    }
}
