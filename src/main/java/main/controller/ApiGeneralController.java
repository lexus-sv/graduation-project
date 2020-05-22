package main.controller;

import main.InitInfo;
import main.model.GlobalSettings;
import main.model.Tag;
import main.model.TagToPost;
import main.model.response.Tags;
import main.model.response.ViewModelFactory;
import main.repository.GlobalSettingsRepository;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.TagToPostRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ApiGeneralController {

    @Autowired
    private GlobalSettingsRepository settingsRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/api/init")
    public ResponseEntity<InitInfo> init() {
        return new ResponseEntity(new InitInfo(), HttpStatus.OK);
    }

    @GetMapping("/api/settings")
    public ResponseEntity getSettings() {
        List<GlobalSettings> settings = new ArrayList<>();
        Iterable<GlobalSettings> settingsIterable = settingsRepository.findAll();
        settingsIterable.forEach(settings::add);
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
        Iterable<Tag> tagIterable;
        if (query.length() != 0) tagIterable = tagRepository.findAllByNameContaining(query);
        else tagIterable = tagRepository.findAll();
        tagIterable.forEach(tagList::add);
        Tags responseBody = ViewModelFactory.getTags(tagList);
        return new ResponseEntity(responseBody, HttpStatus.OK);
    }
}