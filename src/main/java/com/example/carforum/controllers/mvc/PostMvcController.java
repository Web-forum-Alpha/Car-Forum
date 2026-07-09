package com.example.carforum.controllers.mvc;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Post;
import com.example.carforum.models.PostDetailsDto;
import com.example.carforum.services.CommentService;
import com.example.carforum.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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


}
