package main.service;

import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.api.general.StatisticsResponse;
import main.api.general.profile.ProfileEditRequest;
import main.api.general.profile.ProfileEditResponse;
import main.model.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

    User findByEmail(String email);

    User findById(int id);

    void delete(int id);

    AuthResponse register(RegisterUserRequest request);

    User save(User user);

    ProfileEditResponse edit(ProfileEditRequest request, User user);

    StatisticsResponse getUserStatistics(User user);
}
