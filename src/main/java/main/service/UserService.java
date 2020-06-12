package main.service;

import main.api.MyStatisticsResponse;
import main.api.ProfileEditRequest;
import main.api.ProfileEditResponse;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.model.User;

import java.util.HashMap;
import java.util.List;

public interface UserService {

    List<User> getAll();

    User findByEmail(String email);

    User findById(int id);

    void delete(int id);

    AuthResponse register(RegisterUserRequest request);

    User save(User user);

    ProfileEditResponse edit(ProfileEditRequest request, User user);

    MyStatisticsResponse getUserStatistics(User user);
}
