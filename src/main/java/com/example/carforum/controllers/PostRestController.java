package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Post;
import com.example.carforum.models.PostDto;
import com.example.carforum.models.User;
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
    private final ModelMapper mapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PostRestController(PostService postService, ModelMapper mapper, AuthenticationHelper authenticationHelper) {
        this.postService = postService;
        this.mapper = mapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping()
    public List<Post> getAll(HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        return postService.getAll();
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


}