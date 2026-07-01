package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.models.User;
import com.example.carforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final String DUPLICATE_ERROR_MESSAGE = "User with %s %s already exists!";
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public User getById(int id) {
        return userRepository.getById(id);
    }

    @Override
    public User getByUsername(String userName) {
        return userRepository.getByUsername(userName);
    }

    @Override
    public void create(User user) {
        String email = user.getEmail();
        String username = user.getUsername();
        if (userRepository.getByUsername(username) != null) {
            throw new EntityDuplicateException(String.format(DUPLICATE_ERROR_MESSAGE, "username", username));
        }

        if (userRepository.getByEmail(email) != null) {
            throw new EntityDuplicateException(String.format(DUPLICATE_ERROR_MESSAGE, "email", email));
        }
        userRepository.create(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public void update(User userToUpdate) {
        userRepository.update(userToUpdate);
    }
}
