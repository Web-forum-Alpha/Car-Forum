package com.example.carforum.controllers.mvc;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.models.User;
import com.example.carforum.services.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping()
public class HomeController {

    private final PostService postService;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public HomeController(PostService postService,
                          AuthenticationHelper authenticationHelper){
        this.postService = postService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping()
    public String showHomePage(Model model, HttpSession session){

        User user;
        try {
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){

            user = null;
        }

        model.addAttribute("user", user);
        model.addAttribute("recentPosts", postService.getTenMostRecentPosts());
        model.addAttribute("mostCommentedPosts", postService.getTenMostCommentedPosts());

        return "HomeView";
    }
}
