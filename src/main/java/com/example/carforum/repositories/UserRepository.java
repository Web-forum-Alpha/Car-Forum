package com.example.carforum.repositories;

import com.example.carforum.models.User;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserRepository {
    List<User> getAll();

    long getCountUsers();

    User getById(int id);

    User getByUsername(String userName);

    User getByEmail(String email);

    List<User> search(String username, String email, String firstName);

    @Transactional
    void create(User user);

    @Transactional
    void delete(User user);

    @Transactional
    void update(User user);
}
