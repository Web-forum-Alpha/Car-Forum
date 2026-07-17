package com.example.carforum.controllers.mvc;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.LoginDto;
import com.example.carforum.models.User;
import com.example.carforum.models.UserCreateDto;
import com.example.carforum.models.UserUpdateDto;
import com.example.carforum.services.SupabaseStorageService;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserMvcController {
    private static final String ALREADY_LOGGED_IN = "You are already logged in! Logout first!";
    private static final String LOGIN_CREDENTIALS_ERROR_MESSAGE = "Invalid username or password!";
    private static final String ACCESS_ERROR_MESSAGE = "You are not authorized to browse user information.";

    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final ModelMapper modelMapper;
    private final SupabaseStorageService supabaseStorageService;

    public UserMvcController(UserService userService, AuthenticationHelper authenticationHelper, ModelMapper modelMapper, SupabaseStorageService supabaseStorageService) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.modelMapper = modelMapper;
        this.supabaseStorageService = supabaseStorageService;
    }

    @GetMapping("/admin")
    public String admin(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName) {

        if (!authenticationHelper.isLoggedIn(session) || !authenticationHelper.isAdmin(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ACCESS_ERROR_MESSAGE);
            return "ErrorView";
        }

        List<User> users;

        if ((username == null || username.isBlank()) && (email == null || email.isBlank()) && (firstName == null || firstName.isBlank())) {
            users = userService.getAll();
        } else {
            users = userService.search(username, email, firstName);
        }
        model.addAttribute("users", users);

        return "AdminPanelView";
    }

    @PostMapping("/{userId}/block")
    public String block(@PathVariable int userId, HttpSession session, Model model) {
        if (!authenticationHelper.isLoggedIn(session) || !authenticationHelper.isAdmin(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ACCESS_ERROR_MESSAGE);
            return "ErrorView";
        }

        User user = authenticationHelper.getCurrentUser(session);
        User userToUpdate = userService.getById(userId);
        userService.setBlock(userToUpdate, user, true);

        return "redirect:/users/admin";
    }

    @PostMapping("/{userId}/unblock")
    public String unblock(@PathVariable int userId, HttpSession session, Model model) {
        if (!authenticationHelper.isLoggedIn(session) || !authenticationHelper.isAdmin(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ACCESS_ERROR_MESSAGE);
            return "ErrorView";
        }

        User user = authenticationHelper.getCurrentUser(session);
        User userToUpdate = userService.getById(userId);
        userService.setBlock(userToUpdate, user, false);

        return "redirect:/users/admin";
    }

    @PostMapping("/{userId}")
    public String deleteById(@PathVariable int userId, HttpSession session, Model model) {
        if (!authenticationHelper.isLoggedIn(session) || !authenticationHelper.isAdmin(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ACCESS_ERROR_MESSAGE);
            return "ErrorView";
        }
        User user = userService.getById(userId);
        userService.delete(user);
        return "redirect:/users/admin";
    }

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        if (authenticationHelper.isLoggedIn(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }
        model.addAttribute("userCreateDto", new UserCreateDto());

        return "RegisterView";
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if (authenticationHelper.isLoggedIn(session)) {
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

        if (bindingResult.hasErrors()) {
            return "RegisterView";
        }

        User user = modelMapper.fromDtoCreate(userCreateDto);

        List<User> usersByEmail = userService.search(null, user.getEmail(), null);
        List<User> usersByUsername = userService.search(user.getUsername(), null, null);

        if (!usersByEmail.isEmpty() || !usersByUsername.isEmpty()) {
            String errorMessage = usersByEmail.isEmpty() ? "Username already exists!" : "Email already exists!";
            model.addAttribute("errorMessage", errorMessage);
            return "RegisterView";
        }

        userService.create(user);
        return "redirect:/users/login";
    }


    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDto") LoginDto loginDto, BindingResult bindingResult, Model model, HttpSession session) {
        if (authenticationHelper.isLoggedIn(session)) {
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.value());
            model.addAttribute("error", ALREADY_LOGGED_IN);
            return "ErrorView";
        }

        if (bindingResult.hasErrors()) {
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
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        try {
            User currentUser = authenticationHelper.getCurrentUser(session);
            UserUpdateDto dto = modelMapper.toDtoUpdate(currentUser);

            String profilePictureUrl = null;

            if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isBlank()) {
                profilePictureUrl = "https://ktsfrevxaxezsmyuuier.supabase.co/storage/v1/object/public/uploads/" + currentUser.getProfilePicturePath();
            }
            model.addAttribute("userUpdateDto", dto);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("user", currentUser);
            model.addAttribute("profilePictureUrl", profilePictureUrl);
        } catch (ResponseStatusException e) {
            return "redirect:/users/login";
        }
        return "ProfileView";
    }

    @PostMapping("/profile")
    public String profile(@Valid @ModelAttribute("userUpdateDto") UserUpdateDto userUpdateDto,
                          BindingResult bindingResult,
                          HttpSession session,
                          Model model) {

        User currentUser = authenticationHelper.getCurrentUser(session);
        User userToUpdate = userService.getById(currentUser.getId());

        if (userToUpdate == null) {
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.value());
            model.addAttribute("error", "User not found.");
            return "ErrorView";
        }

        User userFromDto = modelMapper.fromDtoUpdate(userToUpdate, userUpdateDto);

        List<User> usersByEmail = userService.search(null, userFromDto.getEmail(), null);

        if (!usersByEmail.isEmpty()) {
            bindingResult.rejectValue("email", "email", "Email already exists.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            return "ProfileView";
        }

        userService.update(userFromDto);

        model.addAttribute("currentUser", userFromDto);

        return "redirect:/users/profile";
    }

    @PostMapping("/profile/picture")
    public String uploadPicture(
            @RequestParam("picture") MultipartFile picture,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws IOException {

        try {
            User currentUser = authenticationHelper.getCurrentUser(session);
            String picturePath = supabaseStorageService.uploadFile(picture, currentUser.getId());
            currentUser.setProfilePicturePath(picturePath);
            userService.update(currentUser);
            session.setAttribute("currentUser", currentUser);
        } catch (ResponseStatusException e) {
            return "redirect:/users/login";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile picture updated successfully.");

        return "redirect:/users/profile";
    }

}

