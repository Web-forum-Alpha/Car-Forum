package com.example.carforum.repositories;

import com.example.carforum.models.User;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    User getById(int id);

    User getByUsername(String userName);

    User getByEmail(String email);

    void create(User user);

    @Transactional
    void delete(User user);
}
