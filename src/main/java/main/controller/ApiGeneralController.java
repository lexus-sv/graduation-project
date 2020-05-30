package main.controller;

import main.InitInfo;
import main.model.GlobalSettings;
import main.model.Tag;
import main.model.User;
import main.api.response.Tags;
import main.api.response.ViewModelFactory;
import main.repository.GlobalSettingsRepository;
import main.repository.TagRepository;
import main.service.AuthService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class ApiGeneralController {

    private final String fileUploadFolder = "./upload/";
    private final String outputPathFolder = "/image/";

    @Autowired
    private GlobalSettingsRepository settingsRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/api/init")
    public ResponseEntity<InitInfo> init() {
        return new ResponseEntity(new InitInfo(), HttpStatus.OK);
    }

    @GetMapping("/api/settings")
    public ResponseEntity getSettings() {
        List<GlobalSettings> settings = settingsRepository.findAll();
        System.out.println(settings);
        JSONObject responseBody = new JSONObject();
        settings.forEach(s -> responseBody.put(s.getCode(), s.isValue()));
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @GetMapping(value = "/api/tag")
    public ResponseEntity tag(
            @RequestParam(name = "query", defaultValue = "", required = false) String query
    ) {
        List<Tag> tagList = new ArrayList<>();
        if (query.length() != 0) tagList = tagRepository.findAllByNameContaining(query);
        else tagList = tagRepository.findAll();
        Tags responseBody = ViewModelFactory.getTags(tagList);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }

    @PostMapping(value = "/api/image")
    public String uploadImage(@RequestParam MultipartFile image) {
        User user = authService.getCurrentUser(RequestContextHolder.currentRequestAttributes().getSessionId());
        if (user != null) {
            return saveImage(image, generatePath(2, 3));
        }
        return null;
    }

    @GetMapping(value = "/image/{rootDir:[a-z]+}-{childDir:[a-z]+}-{childDirSecond:[a-z]+}/{filename}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String rootDir,
            @PathVariable String childDir,
            @PathVariable String childDirSecond,
            @PathVariable String filename) throws IOException {
        File file = new File(fileUploadFolder+rootDir+"/"+childDir+"/"+childDirSecond+"/"+filename);
        return ResponseEntity.ok(Files.readAllBytes(file.toPath()));
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
        return outputPathFolder + path.replace("/", "-").substring(0, path.length()-1) +"/"+ fileName;
    }
}