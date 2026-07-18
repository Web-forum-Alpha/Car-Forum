package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Comment;
import com.example.carforum.models.CommentDto;
import com.example.carforum.models.User;
import com.example.carforum.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Manage comments", description = "CRUD operations for Comments")
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

    @Operation(summary = "Returns all comments")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping()
    public List<Comment> getAll(HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        return service.getAll();
    }

    @Operation(summary = "Returns comment by Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping("/{id}")
    public Comment getById(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);

        return service.getById(id);

    }

    @Operation(summary = "Returns all comments by User's Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping("/user/{id}")
    public List<Comment> getAllByUserId(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        return service.getByUserId(id);
    }

    @Operation(summary = "Returns all comments by Post's Id")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping("/post/{id}")
    public List<Comment> getAllByPostId(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        return service.getByPostId(id);
    }

    @Operation(summary = "Creates a comment")
    @ApiResponse(responseCode = "200", description = "Successes")
    @PostMapping()
    public void create(@Valid @RequestBody CommentDto commentDto, HttpSession session) {
        authenticationHelper.getCurrentUser(session);
        Comment comment = mapper.fromDtoCreate(commentDto);
        service.create(comment);
    }

    @Operation(summary = "Updates a comment by its Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody CommentDto commentDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        Comment comment = mapper.fromDtoUpdate(id, commentDto);
        if (authenticationHelper.isLoggedInNonAdmin(session) && user.getId() != comment.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
        }
        service.update(comment, user);
    }

    @Operation(summary = "Deletes a comment by its Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);
        Comment comment = service.getById(id);
        if (authenticationHelper.isLoggedInNonAdmin(session) && user.getId() != comment.getUser().getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_ERROR_MESSAGE);
        }
        service.deleteById(id, user);
    }
}
