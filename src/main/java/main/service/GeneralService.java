package main.service;

import main.InitInfo;
import main.api.general.ModerationRequest;
import main.api.general.StatisticsResponse;
import main.api.general.calendar.CalendarResponse;
import main.api.general.profile.ProfileEditRequest;
import main.api.general.profile.ProfileEditResponse;
import main.api.post.comment.AddCommentRequest;
import main.api.post.tag.Tags;
import main.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

public interface GeneralService {
    InitInfo init();

    HashMap<String, Boolean> getSettings();

    Tags getTags(String query);

    String uploadImage(MultipartFile file);

    byte[] getImageFromStorage(String path);

    HashMap<Object, Object> addComment(AddCommentRequest request, User user);

    void moderate(ModerationRequest request, User user);

    CalendarResponse getCalendar(Integer year);

    ProfileEditResponse editProfile(ProfileEditRequest request, User user);

    StatisticsResponse getMyStatistics(User user);

    StatisticsResponse getAllStatistics();

    void editSettings(HashMap<String, Boolean> request);
}
