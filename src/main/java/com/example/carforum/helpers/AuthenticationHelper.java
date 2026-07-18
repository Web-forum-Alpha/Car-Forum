package com.example.carforum.helpers;

import com.example.carforum.exceptions.AuthenticationException;
import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.models.User;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            throw new AuthorizationException(USER_NOT_LOGGED_IN_MESSAGE);
        }
        String password = userFromSession.getPassword();
        String username = userFromSession.getUsername();
        User userFromDb = userService.getByUsername(username);

        if (userFromDb == null || !userFromDb.getPassword().equals(password)) {
            throw new AuthenticationException(INVALID_AUTHENTICATION_ERROR);
        }
        return userFromDb;
    }

    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("currentUser") != null;
    }

    public boolean isAdmin(HttpSession session) {
        User user = getCurrentUser(session);
        return user.isAdmin();
    }

    public boolean isBlocked(User user){
        return user.isBlocked();
    }

    public boolean isLoggedInNonAdmin(HttpSession session){
        return isLoggedIn(session) && !isAdmin(session);
    }

}
