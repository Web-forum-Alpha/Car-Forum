package com.example.carforum.controllers.mvc;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.*;
import com.example.carforum.services.CommentService;
import com.example.carforum.services.LikeService;
import com.example.carforum.services.PostService;
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
    private final LikeService likeService;
    private final ModelMapper mapper;
    private final AuthenticationHelper authenticationHelper;

    @Autowired
    public PostMvcController(PostService postService,
                             CommentService commentService,
                             LikeService likeService,
                             ModelMapper mapper,
                             AuthenticationHelper authenticationHelper){
        this.postService = postService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.mapper = mapper;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping
    public String getAllPosts(@RequestParam(required = false) String title,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) Integer likes,
                              @RequestParam(required = false) Integer comments,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String orderBy,
                              Model model, HttpSession session){

        User user;
        try {
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){

            return "redirect:/users/login";
        }

        FilterOptions filterOptions = new FilterOptions(title, username,likes, comments, sortBy, orderBy);

        model.addAttribute("user", user);
        model.addAttribute("posts", postService.getAll(filterOptions));
        return "PostsView";
    }

    @GetMapping("/{postId}")
    public String getPostById(@PathVariable int postId, Model model, HttpSession session){

        User user;
       try{
           user = authenticationHelper.getCurrentUser(session);
       }catch (ResponseStatusException e){

           return "redirect:/users/login";
       }

        try{
            Post post = postService.getById(postId);

            PostDetailsDto dto = mapper.toDto(
                    post,
                    commentService.getByPostId(postId),
                    likeService.getLikesCount(postId),
                    likeService.isLikedByUser(postId,user.getId())
            );

            model.addAttribute("user", user);
            model.addAttribute("postDetails", dto);
            model.addAttribute("commentDto", new CommentDto());

            return "PostView";
        }catch (EntityNotFoundException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            return "ErrorView";
        }
    }

    @GetMapping("/new")
    public String showCreatePostPage(Model model, HttpSession session){

        User user;
        try{

            user = authenticationHelper.getCurrentUser(session);

        }catch (ResponseStatusException e){

            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
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
            return "redirect:/users/login";
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
        }catch (AuthorizationException e){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.getReasonPhrase());
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
            return "redirect:/users/login";
        }

        try{
            Post post = postService.getById(postId);

            //TODO Fix it later
            if (!post.getUser().getUsername().equals(user.getUsername())){

                return "redirect:/posts";
            }

            PostDto dto = mapper.toDtoUpdate(postId, post);

            model.addAttribute("user", user);
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

            return "redirect:/users/login";
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

    @PostMapping("/{postId}/like")
    public String likeAction(@PathVariable int postId, Model model , HttpSession session){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){

            return "redirect:/users/login";
        }

        try{
            Like like = mapper.fromDto(postId, user.getId());

            likeService.interactionWithLikeButton(like);
            return "redirect:/posts/{postId}";

        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }
    }

    @PostMapping("/{postId}/comment")
    public String createComment(@PathVariable int postId, HttpSession session, @ModelAttribute CommentDto commentDto, Model model){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/users/login";
        }

        try{
            commentDto.setPostId(postId);
            commentDto.setUserId(user.getId());
            Comment comment = mapper.fromDtoCreate(commentDto);
            commentService.create(comment);
            return "redirect:/posts/{postId}";

        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }catch (AuthorizationException e){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }

    }


    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable int postId,Model model, HttpSession session){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/users/login";
        }

        try{
            postService.deleteById(postId, user);

            return "redirect:/posts";

        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }

    }

    @GetMapping("/{postId}/comments/{commentId}/edit")
    public String showEditCommentPage(@PathVariable int postId, @PathVariable int commentId,
                                      Model model,
                                      HttpSession session){

        User user;
        try {
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/users/login";
        }

        Comment comment = commentService.getById(commentId);

        if (!(comment.getUser().getId() == user.getId() || user.isAdmin())){

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to edit this comment");
        }
        CommentDto commentDto = mapper.toDtoUpdate(comment);

        model.addAttribute("commentDto", commentDto);
        model.addAttribute("postId", postId);
        model.addAttribute("commentId", commentId);

        return "EditCommentView";

    }


    @PostMapping("/{postId}/comments/{commentId}/edit")
    public String editComment(@PathVariable int postId, @PathVariable int commentId,
                                HttpSession session, Model model,
                                @Valid @ModelAttribute("commentDto") CommentDto commentDto,
                              BindingResult bindingResult){

        User user;
        try {
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/users/login";
        }

        if (bindingResult.hasErrors()){
            return "EditCommentView";
        }
        try{
            Comment comment = commentService.getById(commentId);
            comment.setContent(commentDto.getContent());
            commentService.update(comment, user);

            return "redirect:/posts/{postId}";
        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }catch (AuthorizationException e){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";

        }


    }

    @PostMapping("/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable int postId, @PathVariable int commentId,
                                HttpSession session, Model model){

        User user;
        try{
            user = authenticationHelper.getCurrentUser(session);
        }catch (ResponseStatusException e){
            return "redirect:/users/login";
        }

        try {
            commentService.deleteById(commentId, user);
            return "redirect:/posts/{postId}";

        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";
        }catch (AuthorizationException e){
            model.addAttribute("statusCode", HttpStatus.FORBIDDEN.getReasonPhrase());
            model.addAttribute("error", e.getMessage());
            return "ErrorView";

        }

    }


}
