package com.example.carforum.services;

import com.example.carforum.models.User;
import com.example.carforum.models.UserUpdateDto;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(int id);

    User getByUsername(String userName);

    void create(User user);

    void delete(User user);
    void update(User userToUpdate);
}
