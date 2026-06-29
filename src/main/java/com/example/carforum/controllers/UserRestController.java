package com.example.carforum.controllers;

import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.LoginDto;
import com.example.carforum.models.User;
import com.example.carforum.models.UserDto;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private final String LOGIN_CREDENTIALS_ERROR_MESSAGE = "Invalid username or password!";
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserRestController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable int userId) {
        return userService.getById(userId);
    }

    //Adding for testing purposes
    @GetMapping("/search/{username}")
    public User getByUsername(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @PostMapping("/register")
    public void create(@Valid @RequestBody UserDto userDto) {
        User user = modelMapper.fromDtoCreate(userDto);
        userService.create(user);
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginDto loginDto, HttpSession session) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        User user = userService.getByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_CREDENTIALS_ERROR_MESSAGE);
        }
        session.setAttribute("currentUser", user);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

}
