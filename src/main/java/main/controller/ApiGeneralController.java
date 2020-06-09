package main.controller;

import main.InitInfo;
import main.api.auth.response.ResultResponse;
import main.api.post.comment.AddCommentRequest;
import main.api.general.ModerationRequest;
import main.model.User;
import main.service.AuthServiceImpl;
import main.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class ApiGeneralController {

    private final String fileUploadFolder = "./upload/";
    private final String outputPathFolder = "/image/";


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
            @PathVariable String filename) throws IOException {
        return ResponseEntity.ok(generalService.getImageFromStorage(fileUploadFolder + rootDir + "/" + childDir + "/" + childDirSecond + "/" + filename));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/api/comment")
    public ResponseEntity addComment(
            @CookieValue(value = "token", defaultValue = "invalid")  String token,
            @RequestBody AddCommentRequest request) {
        return ResponseEntity.ok(generalService.addComment(request, authService.getAuthorizedUser(token)));
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
    //@TODO post api/moderation
    //@TODO post api/auth/restore
    //@TODO post api/auth/password
    //@TODO post api/profile/my
    //@TODO get api/statistics/my
    //@TODO get api/statistics/all
    //@TODO put api/settings
    //@TODO change all return null on ResponseEntity.unauthorized(null)

}