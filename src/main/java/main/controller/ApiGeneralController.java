package main.controller;

import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import main.InitInfo;
import main.api.auth.response.ResultResponse;
import main.api.general.ModerationRequest;
import main.api.general.StatisticsResponse;
import main.api.general.profile.ProfileEditRequest;
import main.api.post.comment.AddCommentRequest;
import main.service.GeneralService;
import main.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ApiGeneralController {

  private final String fileUploadFolder = "./upload/";


  private final GeneralService generalService;
  private final AuthServiceImpl authService;

  @Autowired
  public ApiGeneralController(GeneralService generalService, AuthServiceImpl authService) {
    this.generalService = generalService;
    this.authService = authService;
  }

  @GetMapping("/api/init")
  public ResponseEntity<InitInfo> init() {
    return ResponseEntity.ok(generalService.init());
  }

  @GetMapping("/api/settings")
  public ResponseEntity getSettings() {
    HashMap<String, Boolean> settings = generalService.getSettings();
    return ResponseEntity.ok(settings);
  }

  @GetMapping(value = "/api/tag")
  public ResponseEntity tag(
      @RequestParam(name = "query", defaultValue = "", required = false) String query
  ) {
    return ResponseEntity.ok(generalService.getTags(query));
  }

  @Secured("ROLE_USER")
  @PostMapping(value = "/api/image")
  public String uploadImage(@RequestParam MultipartFile image) {
    return generalService.uploadImage(image);
  }

  @GetMapping(value = "/image/{rootDir:[a-z]+}-{childDir:[a-z]+}-{childDirSecond:[a-z]+}/{filename}")
  public ResponseEntity<byte[]> getImage(
      @PathVariable String rootDir,
      @PathVariable String childDir,
      @PathVariable String childDirSecond,
      @PathVariable String filename) {
    return ResponseEntity.ok(generalService
        .getImageFromStorage(rootDir + "/" + childDir + "/" + childDirSecond + "/" + filename));
  }

  @Secured("ROLE_USER")
  @PostMapping(value = "/api/comment")
  public ResponseEntity addComment(
      @CookieValue(value = "token", defaultValue = "invalid") String token,
      @RequestBody AddCommentRequest request) {
    return ResponseEntity
        .ok(generalService.addComment(request, authService.getAuthorizedUser(token)));
  }

  @GetMapping("/api/calendar")
  public ResponseEntity getCalendar(
      @RequestParam(name = "year", defaultValue = "", required = false) int year
  ) {
    return ResponseEntity.ok(generalService.getCalendar(year));
  }

  @Secured("ROLE_MODERATOR")
  @PostMapping("/api/moderation")
  public ResponseEntity<?> moderation(
      @CookieValue(value = "token", defaultValue = "invalid") String token,
      @RequestBody ModerationRequest request) {
    generalService.moderate(request, authService.getAuthorizedUser(token));
    return ResponseEntity.ok(new ResultResponse(true));
  }

  @Secured("ROLE_USER")
  @PostMapping(value = "/api/profile/my", headers = {"Content-Type=application/json"})
  public ResponseEntity<?> editProfile(
      @CookieValue(name = "token", defaultValue = "invalid") String token,
      @RequestBody ProfileEditRequest request,
      HttpServletResponse response) {
    System.out.println(request);
    return ResponseEntity
        .ok(generalService.editProfile(request, authService.getAuthorizedUser(token), response));
  }


  @Secured("ROLE_USER")
  @PostMapping(value = "/api/profile/my", headers = "Content-Type=multipart/form-data")
  public ResponseEntity<?> editProfileWithPhoto(
      @CookieValue(name = "token", defaultValue = "invalid") String token,
      @Valid @ModelAttribute ProfileEditRequest request,
      HttpServletResponse response) {
    System.out.println(((MultipartFile) request.getPhoto()).getOriginalFilename());
    return ResponseEntity
        .ok(generalService.editProfile(request, authService.getAuthorizedUser(token), response));
  }

  @Secured("ROLE_USER")
  @GetMapping("/api/statistics/my")
  public ResponseEntity<?> getMyStatistics(
      @CookieValue(name = "token", defaultValue = "invalid") String token) {
    return ResponseEntity.ok(generalService.getMyStatistics(authService.getAuthorizedUser(token)));
  }

  @GetMapping("/api/statistics/all")
  public ResponseEntity<StatisticsResponse> getGlobalStatistics() {
    return ResponseEntity.ok(generalService.getAllStatistics());
  }

  @Secured("ROLE_MODERATOR")
  @PutMapping("/api/settings")
  public ResponseEntity<?> editSettings(@RequestBody HashMap<String, Boolean> request) {
    generalService.editSettings(request);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}