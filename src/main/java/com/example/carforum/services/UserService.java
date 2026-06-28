package com.example.carforum.services;

import com.example.carforum.models.User;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(int id);

    User getByUsername(String userName);
}
