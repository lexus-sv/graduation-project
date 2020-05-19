package main.controller;

import main.InitInfo;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.model.response.ViewModelFactory;
import main.model.response.PostModelType;
import main.model.response.UserModelType;
import main.model.response.post.PostBehavior;
import main.repository.PostRepository;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ApiGeneralController {

    @GetMapping("/api/init")
    public ResponseEntity<InitInfo> init() {
        return new ResponseEntity(new InitInfo(), HttpStatus.OK);
    }

}