package com.example.carforum.services;

import com.example.carforum.models.User;
import com.example.carforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAll(){
        return userRepository.getAll();
    }

    @Override
    public User getById(int id){
        return userRepository.getById(id);
    }

    @Override
    public User getByUsername(String userName){
        return userRepository.getByUsername(userName);
    }
}
