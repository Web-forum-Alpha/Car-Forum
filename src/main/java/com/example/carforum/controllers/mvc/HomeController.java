package com.example.carforum.controllers.mvc;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.models.User;
import com.example.carforum.services.PostService;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public HomeController(PostService postService,
                          UserService userService,
                          AuthenticationHelper authenticationHelper){
        this.postService = postService;
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping()
    public String showHomePage(Model model, HttpSession session){

        User user;

        if (authenticationHelper.isLoggedIn(session)){
            user = authenticationHelper.getCurrentUser(session);
        }else {
            user = null;
        }


        model.addAttribute("postsCount", postService.getCountPosts());
        model.addAttribute("usersCount",userService.getCountUsers());
        model.addAttribute("user", user);
        model.addAttribute("recentPosts", postService.getTenMostRecentPosts());
        model.addAttribute("mostCommentedPosts", postService.getTenMostCommentedPosts());

        return "HomeView";
    }
}
