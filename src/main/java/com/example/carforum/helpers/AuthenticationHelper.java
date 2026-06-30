package com.example.carforum.helpers;

import com.example.carforum.models.User;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationHelper {
    private static final String USER_NOT_LOGGED_IN_MESSAGE = "You are not logged in.";
    private static final String INVALID_AUTHENTICATION_ERROR = "Invalid authentication.";
    private final UserService userService;

    @Autowired
    public AuthenticationHelper(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser(HttpSession session) {
        User userFromSession = (User) session.getAttribute("currentUser");
        if (userFromSession == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_LOGGED_IN_MESSAGE);
        }
        String password = userFromSession.getPassword();
        String username = userFromSession.getUsername();
        User userFromDb = userService.getByUsername(username);

        if (userFromDb == null || !userFromDb.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_AUTHENTICATION_ERROR);
        }
        return userFromDb;
    }

    public boolean userIsLoggedIn(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }

    public boolean userIsAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        return user.isAdmin();
    }

}
