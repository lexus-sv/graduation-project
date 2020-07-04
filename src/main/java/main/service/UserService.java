package main.service;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import main.api.auth.request.RegisterUserRequest;
import main.api.auth.response.AuthResponse;
import main.api.general.StatisticsResponse;
import main.api.general.profile.ProfileEditRequest;
import main.api.general.profile.ProfileEditResponse;
import main.model.User;

public interface UserService {

  List<User> getAll();

  User findByEmail(String email);

  User findById(int id);

  void delete(int id);

  AuthResponse register(RegisterUserRequest request);

  User save(User user);

  ProfileEditResponse edit(ProfileEditRequest request, User user, HttpServletResponse response);

  StatisticsResponse getUserStatistics(User user);
}
