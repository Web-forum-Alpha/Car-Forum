package com.example.carforum.controllers;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.LoginDto;
import com.example.carforum.models.User;
import com.example.carforum.models.UserCreateDto;
import com.example.carforum.models.UserUpdateDto;
import com.example.carforum.services.SupabaseStorageServiceImpl;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    private static final String LOGIN_CREDENTIALS_ERROR_MESSAGE = "Invalid username or password!";
    private static final String ALREADY_LOGGED_IN_ERROR_MESSAGE = "You are already logged in!";
    private static final String ALREADY_LOGGED_IN_REGISTER_ERROR_MESSAGE = "You are already logged in, to register another user, logout first!";
    private static final String ALREADY_LOGGED_OUT_ERROR_MESSAGE = "You are already logged out!";
    private static final String USERID_NOT_FOUND = "User with id %d is not found!";
    private static final String USERNAME_NOT_FOUND = "User with username %s is not found!";
    private static final String ACCESS_ERROR_MESSAGE = "You are not authorized to browse user information.";
    private static final String ACCESS_UPDATE_ERROR_MESSAGE = "You are not authorized to update other user's information.";

    private final UserService userService;
    private final SupabaseStorageServiceImpl supabaseStorageService;
    private final ModelMapper modelMapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public UserRestController(UserService userService, ModelMapper modelMapper, AuthenticationHelper authenticationHelper, SupabaseStorageServiceImpl supabaseStorageService) {
        this.userService = userService;
        this.supabaseStorageService = supabaseStorageService;
        this.modelMapper = modelMapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public List<User> getAll(HttpSession session) {
        if (authenticationHelper.isLoggedInNonAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
        }
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable int userId, HttpSession session) {
        if (authenticationHelper.isLoggedInNonAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }
        return user;
    }

    @PutMapping("/{userId}")
    public void updateById(@PathVariable int userId, @Valid @RequestBody UserUpdateDto userUpdateDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        if (userId != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_UPDATE_ERROR_MESSAGE);
        }
        User userToUpdate = userService.getById(userId);

        if (userToUpdate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }

        userToUpdate = modelMapper.fromDtoUpdate(userToUpdate, userUpdateDto);

        userService.update(userToUpdate);

        session.setAttribute("currentUser", userToUpdate);
    }

    @PutMapping("/{userId}/block")
    public void blockUser(@PathVariable int userId, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        User userToUpdate = userService.getById(userId);

        if (userToUpdate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }

        userService.setBlock(userToUpdate, user, true);
    }

    @PutMapping("/{userId}/unblock")
    public void unblockUser(@PathVariable int userId, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        User userToUpdate = userService.getById(userId);

        if (userToUpdate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }

        userService.setBlock(userToUpdate, user, false);
    }

    @PostMapping("/{userId}/picture")
    public void uploadPicture(@PathVariable int userId, @RequestParam("picture") MultipartFile picture, HttpSession session) throws IOException {

        User currentUser = authenticationHelper.getCurrentUser(session);

        if (currentUser.getId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String picturePath = supabaseStorageService.uploadFile(picture, userId);

        User user = userService.getById(userId);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }

        user.setProfilePicturePath(picturePath);

        userService.update(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteById(@PathVariable int userId, HttpSession session) {
        if (authenticationHelper.isLoggedInNonAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
        }
        User user = userService.getById(userId);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERID_NOT_FOUND, userId));
        }

        userService.delete(user);
    }

    //Adding for testing purposes
    @GetMapping("/search/{username}")
    public User getByUsername(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String firstName) {

        if(username == null && email == null && firstName == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one search parameter must be provided.");
        }

        User user = userService.search(username,email,firstName);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(USERNAME_NOT_FOUND, username));
        }

        return user;
    }



    @PostMapping("/register")
    public void create(@Valid @RequestBody UserCreateDto userCreateDto, HttpSession session) {
        if (authenticationHelper.isLoggedInNonAdmin(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ALREADY_LOGGED_IN_REGISTER_ERROR_MESSAGE);
        }
        User user = modelMapper.fromDtoCreate(userCreateDto);
        userService.create(user);
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody LoginDto loginDto, HttpSession session) {
        if (authenticationHelper.isLoggedIn(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ALREADY_LOGGED_IN_ERROR_MESSAGE);
        }
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
        if (!authenticationHelper.isLoggedIn(session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ALREADY_LOGGED_OUT_ERROR_MESSAGE);
        }
        session.invalidate();
    }

}
