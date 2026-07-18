package com.example.carforum.controllers;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.*;
import com.example.carforum.services.LikeService;
import com.example.carforum.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Manage posts", description = "CRUD operations for Posts")
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

    @Operation(summary = "Returns all users. Filter, sorting and ordering is available.")
    @ApiResponse(responseCode = "200", description = "Successes")
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

    @Operation(summary = "Returns a Post by its Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping("/{id}")
    public Post getById(@PathVariable int id, HttpSession session) {
        authenticationHelper.getCurrentUser(session);

        return postService.getById(id);

    }

    @Operation(summary = "Creates a Post.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @PostMapping()
    public void create(@Valid @RequestBody PostDto postDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }

        Post post = mapper.fromDtoCreate(postDto, user);
        postService.create(post);
    }

    @Operation(summary = "Updates a Post. Id required for searching the Post.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody PostDto postDto, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }

        Post post = mapper.fromDtoUpdate(id, postDto);

        postService.update(post, user);

    }

    @Operation(summary = "Deletes a Post by its Id.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, HttpSession session) {
        User user = authenticationHelper.getCurrentUser(session);

        if (authenticationHelper.isBlocked(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, USER_BLOCKED_MESSAGE);
        }


        postService.deleteById(id, user);

    }

    @Operation(summary = "Returns the count of likes of a Post.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @GetMapping("/{postId}/likes")
    public int getPostLikesCount(@PathVariable int postId) {

        return likeService.getLikesCount(postId);

    }

    @Operation(summary = "Creates a like for a Post.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @PostMapping("/{postId}/likes")
    public void crateLike(@PathVariable int postId, HttpSession session) {


        User user = authenticationHelper.getCurrentUser(session);
        Like like = mapper.fromDtoCreate(postId, user);
        likeService.create(like);


    }

    @Operation(summary = "Deletes a like for Post.")
    @ApiResponse(responseCode = "200", description = "Successes")
    @DeleteMapping("/{postId}/likes")
    public void deleteLike(@PathVariable int postId, HttpSession session) {


        User user = authenticationHelper.getCurrentUser(session);
        Like like = mapper.fromDtoDelete(postId, user);
        likeService.delete(like);


    }


}