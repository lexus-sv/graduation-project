package main.service;

import main.InitInfo;
import main.api.request.AddCommentRequest;
import main.api.request.ModerationRequest;
import main.api.response.CalendarResponse;
import main.api.response.Tags;
import main.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.HashMap;

public interface GeneralService {
    InitInfo init();
    HashMap<String, Boolean> getSettings();
    Tags getTags(String query);
    String uploadImage(MultipartFile file);
    byte[] getImageFromStorage(String path);
    HashMap<Object, Object> addComment(AddCommentRequest request, User user);
    void moderate(ModerationRequest request);
    CalendarResponse getCalendar(Integer year);
}
