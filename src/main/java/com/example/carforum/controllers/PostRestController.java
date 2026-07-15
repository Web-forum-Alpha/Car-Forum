package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.*;
import com.example.carforum.services.LikeService;
import com.example.carforum.services.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostRestController {
    private static final String USER_BLOCKED_MESSAGE = "Your user is blocked by the admin, you cannot create/update/delete posts.";
    private final PostService postService;
    private final LikeService likeService;
    private final ModelMapper mapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PostRestController(PostService postService, ModelMapper mapper, AuthenticationHelper authenticationHelper, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
        this.mapper = mapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping()
    public List<Post> getAll(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) Integer likes,
                             @RequestParam(required = false) Integer comments,
                             @RequestParam(required = false) String sortBy,
                             @RequestParam(required = false) String orderBy,
                             HttpSession session) {
        authenticationHelper.getCurrentUser(session);

        FilterOptions filterOptions = new FilterOptions(title, username, likes, comments, sortBy, orderBy);
        return postService.getAll(filterOptions);
    }

    @GetMapping("/{id}")
    public Post getById(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        try {
            return postService.getById(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping()
    public void create(@Valid @RequestBody PostDto postDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }

        Post post = mapper.fromDtoCreate(postDto, user);
        postService.create(post);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody PostDto postDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }

        Post post = mapper.fromDtoUpdate(id, postDto);
        try {
            postService.update(post, user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }

        try {
            postService.deleteById(id, user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{postId}/likes")
    public int getPostLikesCount(@PathVariable int postId){
        try {
            return likeService.getLikesCount(postId);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{postId}/likes")
    public void crateLike(@PathVariable int postId, HttpSession session){

        try{
            User user = authenticationHelper.getCurrentUser(session);
            Like like = mapper.fromDtoCreate(postId, user);
            likeService.create(like);
        }catch (EntityDuplicateException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

    }
    @DeleteMapping("/{postId}/likes")
    public void deleteLike(@PathVariable int postId, HttpSession session){

        try{
            User user = authenticationHelper.getCurrentUser(session);
            Like like = mapper.fromDtoDelete(postId, user);
            likeService.delete(like);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }


}