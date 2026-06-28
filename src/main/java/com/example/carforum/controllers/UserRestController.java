package com.example.carforum.controllers;

import com.example.carforum.models.User;
import com.example.carforum.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll(){
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable int userId){
        return userService.getById(userId);
    }

    //Adding for testing purposes
    @GetMapping("/search/{username}")
    public User getByUsername(@PathVariable String username){
        return userService.getByUsername(username);
    }

}
