package main.controller;

import main.InitInfo;
import main.api.request.AddCommentRequest;
import main.api.request.ModerationRequest;
import main.model.User;
import main.service.AuthService;
import main.service.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class ApiGeneralController {

    private final String fileUploadFolder = "./upload/";
    private final String outputPathFolder = "/image/";


    private final GeneralService generalService;
    private final AuthService authService;

    @Autowired
    public ApiGeneralController(GeneralService generalService, AuthService authService) {
        this.generalService = generalService;
        this.authService = authService;
    }

    @GetMapping("/api/init")
    public ResponseEntity<InitInfo> init() {
        return ResponseEntity.ok(generalService.init());
    }

    @GetMapping("/api/settings")
    public ResponseEntity getSettings() {
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        if(user == null || !user.isModerator())
            return null;

        HashMap<String, Boolean> settings = generalService.getSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping(value = "/api/tag")
    public ResponseEntity tag(
            @RequestParam(name = "query", defaultValue = "", required = false) String query
    ) {
        return ResponseEntity.ok(generalService.getTags(query));
    }

    @PostMapping(value = "/api/image")
    public String uploadImage(@RequestParam MultipartFile image) {
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        if (user != null) {
            return generalService.uploadImage(image);
        }
        return null;
    }

    @GetMapping(value = "/image/{rootDir:[a-z]+}-{childDir:[a-z]+}-{childDirSecond:[a-z]+}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String rootDir,
            @PathVariable String childDir,
            @PathVariable String childDirSecond,
            @PathVariable String filename) throws IOException {
        return ResponseEntity.ok(generalService.getImageFromStorage(fileUploadFolder + rootDir + "/" + childDir + "/" + childDirSecond + "/" + filename));
    }

    @PostMapping(value = "/api/comment")
    public ResponseEntity addComment(@RequestBody AddCommentRequest request) {
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        if (user != null) {
            return ResponseEntity.ok(generalService.addComment(request, user));
        }
        return null;
    }

    @GetMapping("/api/calendar")
    public ResponseEntity getCalendar(
            @RequestParam(name = "year", defaultValue = "", required = false) int year
    ) {
        return ResponseEntity.ok(generalService.getCalendar(year));
    }

    @PostMapping("/api/moderation")
    public void moderation(@RequestBody ModerationRequest request){
        generalService.moderate(request);
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