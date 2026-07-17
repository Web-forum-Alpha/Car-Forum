package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Comment;
import com.example.carforum.models.CommentDto;
import com.example.carforum.models.User;
import com.example.carforum.services.CommentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentRestController {
    private static final String ACCESS_ERROR_MESSAGE = "You are not authorized to update/delete comments from other users";
    private final CommentService service;
    private final ModelMapper mapper;
    private final AuthenticationHelper authenticationHelper;


    public CommentRestController(CommentService service, ModelMapper mapper, AuthenticationHelper authenticationHelper) {
        this.service = service;
        this.mapper = mapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping()
    public List<Comment> getAll(HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Comment getById(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        try {
            return service.getById(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public List<Comment> getAllByUserId(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        try {
            return service.getByUserId(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/post/{id}")
    public List<Comment> getAllByPostId(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        try {
            return service.getByPostId(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping()
    public void create(@Valid @RequestBody CommentDto commentDto, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        Comment comment = mapper.fromDtoCreate(commentDto);
        service.create(comment);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody CommentDto commentDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        try {
            Comment comment = mapper.fromDtoUpdate(id, commentDto);
            if (authenticationHelper.isLoggedInNonAdmin(session) && user.getId() != comment.getUser().getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
            }
            service.update(comment, user);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        try {
            Comment comment = service.getById(id);
            if (authenticationHelper.isLoggedInNonAdmin(session) && user.getId() != comment.getUser().getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        service.deleteById(id, user);
    }
}
