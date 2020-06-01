package main.service.impl;

import lombok.SneakyThrows;
import main.InitInfo;
import main.api.request.AddCommentRequest;
import main.api.request.CalendarObject;
import main.api.request.ModerationRequest;
import main.api.response.CalendarResponse;
import main.api.response.Tags;
import main.api.response.ViewModelFactory;
import main.model.*;
import main.repository.*;
import main.service.AuthService;
import main.service.GeneralService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
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

    private final String fileUploadFolder = "./upload/";
    private final String outputPathFolder = "/image/";

    private final GlobalSettingsRepository settingsRepository;

    private final AuthService authService;

    private final PostRepository postRepository;

    private final PostCommentRepository commentRepository;

    private final TagRepository tagRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public GeneralServiceImpl(GlobalSettingsRepository settingsRepository, AuthService authService, PostRepository postRepository, PostCommentRepository commentRepository, TagRepository tagRepository) {
        this.settingsRepository = settingsRepository;
        this.authService = authService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public InitInfo init() {
        return new InitInfo(title, subtitle, phone, email, copyright, copyrightFrom);
    }

    @Override
    public HashMap<String, Boolean> getSettings() {
        HashMap<String, Boolean> result = new HashMap<>();
        settingsRepository.findAll().forEach(s -> result.put(s.getCode(), s.isValue()));
        return result;
    }

    @Override
    public Tags getTags(String query) {
        List<Tag> tagList = query.length() != 0
                ? tagRepository.findAllByNameContaining(query)
                : tagRepository.findAll();
        return ViewModelFactory.getTags(tagList);
    }

    @Override
    public String uploadImage(MultipartFile image) {
        return saveImage(image, generatePath(2, 3));
    }

    @SneakyThrows
    @Override
    public byte[] getImageFromStorage(String path) {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public HashMap<Object, Object> addComment(AddCommentRequest request, User user) {
        Post post = postRepository.findById(request.getPostId()).orElse(null);
        HashMap<Object, Object> response = new HashMap<>();
        if (request.getParentId() != null) {
            PostComment parentComment = commentRepository.findById(request.getParentId()).orElse(null);

            if (parentComment == null || post == null) {
                return null;
            }

            if (request.getText().length() < 5) {
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
        } else {//if parentId == null
            if (request.getText().length() < 5) {
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
    public void moderate(ModerationRequest request) {
        Post post = postRepository.findById(request.getPostId()).get();
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        post.setModerationStatus(ModerationStatus.getEqualStatus(request.getDecision()));
        post.setModerator(user);
        postRepository.save(post);
    }

    @Override
    public CalendarResponse getCalendar(Integer year) {
        if(year==null){
            year =  new Date().getYear();
        }
        HashMap<String, Long> posts = new HashMap<>();
        postRepository.getCalendarQuery(year).forEach(o -> posts.put(dateFormat.format(o.getDate()), o.getCount()));
        return new CalendarResponse(
                postRepository.getYears(),
                posts
        );
    }

    private String saveImage(MultipartFile image, String hashPath) {
        if (!image.isEmpty()) {
            try {
                String imageName = image.getOriginalFilename();
                final String fileUploadPath = fileUploadFolder + hashPath + imageName;
                byte[] bytes = image.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(fileUploadPath)));
                stream.write(bytes);
                stream.close();

                return getOutputPath(hashPath, imageName);
            } catch (Exception e) {
                return "Вам не удалось загрузить  => " + e.getMessage();
            }
        } else {
            return null;
        }
    }

    private String generatePath(int folderNameLength, int foldersAmount) {
        String symbols = "abcdefghijklmnopqrstuv";
        StringBuilder path = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < foldersAmount; i++) {
            for (int j = 0; j < folderNameLength; j++) {
                int index = (int) (random.nextFloat() * symbols.length());
                path.append(symbols, index, index + 1);
            }
            path.append("/");
        }
        File file = new File(fileUploadFolder + path.toString());
        file.mkdirs();
        return path.toString();
    }

    private String getOutputPath(String path, String fileName) {
        return outputPathFolder + path.replace("/", "-").substring(0, path.length() - 1) + "/" + fileName;
    }
}
