package main.service;

import main.model.User;

import java.util.List;

public interface UserService {

    User register(User user);

    List<User> getAll();

    User findByEmail(String email);

    User findById(int id);

    void delete(int id);

}
