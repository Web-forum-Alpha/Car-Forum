package com.example.carforum.controllers.mvc;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.LoginDto;
import com.example.carforum.models.User;
import com.example.carforum.models.UserCreateDto;
import com.example.carforum.models.UserUpdateDto;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserMvcController {
    private static final String ALREADY_LOGGED_IN = "You are already logged in. Logout first to register another user.";
    private static final String LOGIN_CREDENTIALS_ERROR_MESSAGE = "Invalid username or password!";

    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final ModelMapper modelMapper;

    public UserMvcController(UserService userService, AuthenticationHelper authenticationHelper, ModelMapper modelMapper) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        if(authenticationHelper.isLoggedIn(session)){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }
        model.addAttribute("userCreateDto", new UserCreateDto());

        return "RegisterView";
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if(authenticationHelper.isLoggedIn(session)){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }
        model.addAttribute("loginDto", new LoginDto());

        return "LoginView";
    }

    @PostMapping("/register")
    public String create(@Valid @ModelAttribute("userCreateDto") UserCreateDto userCreateDto, BindingResult bindingResult, Model model, HttpSession session) {
        if (authenticationHelper.isLoggedInNonAdmin(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }

        if(bindingResult.hasErrors()){
            return "RegisterView";
        }

        User user = modelMapper.fromDtoCreate(userCreateDto);

        List <User> usersByEmail = userService.search(null, user.getEmail(), null);
        List <User> usersByUsername = userService.search(user.getUsername(), null, null);

        if (!usersByEmail.isEmpty() || !usersByUsername.isEmpty()) {
            String errorMessage = usersByEmail.isEmpty() ? "Username already exists!" : "Email already exists!";
            model.addAttribute("errorMessage", errorMessage);
            return "RegisterView";
        }

        userService.create(user);
        return "redirect:/users/login";
    }


    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDto") LoginDto loginDto,BindingResult bindingResult, Model model, HttpSession session) {
        if (authenticationHelper.isLoggedIn(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }

        if(bindingResult.hasErrors()){
            return "LoginView";
        }

        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        User user = userService.getByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", LOGIN_CREDENTIALS_ERROR_MESSAGE);
            return "ErrorView";
        }

        session.setAttribute("currentUser", user);
        model.addAttribute("currentUser", user);
        return "redirect:/users/profile";
    }


    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        User currentUser = authenticationHelper.getCurrentUser(session);

        UserUpdateDto dto = modelMapper.toDtoUpdate(currentUser);

        model.addAttribute("userUpdateDto", dto);
        model.addAttribute("currentUser", currentUser);

        return "ProfileView";
    }


