package com.example.carforum.controllers.mvc;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Post;
import com.example.carforum.models.PostDetailsDto;
import com.example.carforum.models.PostDto;
import com.example.carforum.models.User;
import com.example.carforum.services.CommentService;
import com.example.carforum.services.PostService;
import com.example.carforum.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/posts")
public class PostMvcController {

    private final PostService postService;
    private final CommentService commentService;
    private final ModelMapper mapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PostMvcController(PostService postService,
                             CommentService commentService,
                             ModelMapper mapper,
                             AuthenticationHelper authenticationHelper){
        this.postService = postService;
        this.commentService = commentService;
        this.mapper = mapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public String getAllPosts(Model model){

        model.addAttribute("posts", postService.getAll());
        return "PostsView";
    }

    @GetMapping("/{postId}")
    public String getPostById(@PathVariable int postId, Model model){

        try{
            Post post = postService.getById(postId);

            PostDetailsDto dto = mapper.toDto(
                    post,
                    commentService.getByPostId(postId),
                    postService.getLikesCount(postId)
            );

            model.addAttribute("postDetails", dto);

            return "PostView";
        }catch (EntityNotFoundException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            return "ErrorView";
        }
    }

    @GetMapping("/new")
    public String showCreatePostPage(Model model, HttpSession session){

        try{

            authenticationHelper.getCurrentUser(session);

        }catch (ResponseStatusException e){

            return "redirect:/auth/login";
        }

        model.addAttribute("post", new PostDto());
        return "CreatePostView";

    }

    @PostMapping("/new")
    public String createPost(@Valid @ModelAttribute("post") PostDto postDto,
                             BindingResult bindingResult,
                             Model model,
                             HttpSession session){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);

        }catch (ResponseStatusException e){
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()){
            return "CreatePostView";
        }

        try{
            Post post = mapper.fromDtoCreate(postDto, user);
            postService.create(post);
            return "redirect:/posts";
        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }


    }

    @GetMapping("/{postId}/update")
    public String showUpdatePostPage(@PathVariable int postId, Model model,
                                     HttpSession session){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/auth/login";
        }

        try{
            Post post = postService.getById(postId);

            //TODO Fix it later
            if (!post.getUser().getUsername().equals(user.getUsername())){

                return "redirect:/posts";
            }

            PostDto dto = mapper.toDtoUpdate(postId, post);

            model.addAttribute("postId", postId);
            model.addAttribute("post", dto);

            return "PostUpdateView";

        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }

    }

    @PostMapping("/{postId}/update")
    public String updatePost(@PathVariable int postId,
                             @Valid @ModelAttribute("post") PostDto postDto,
                             BindingResult bindingResult,
                             Model model,
                             HttpSession session){
        User user;
        try{
           user = authenticationHelper.getCurrentUser(session);

        }catch (ResponseStatusException e){

            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()){
            return "PostUpdateView";
        }

        try{

            Post post = mapper.fromDtoUpdate(postId, postDto);
            postService.update(post, user);

            return "redirect:/posts";


        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }catch (AuthorizationException e){
            model.addAttribute("statusCode", HttpStatus.UNAUTHORIZED.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }

    }


}
