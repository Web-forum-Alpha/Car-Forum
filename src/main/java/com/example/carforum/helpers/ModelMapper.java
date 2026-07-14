package com.example.carforum.helpers;

import com.example.carforum.models.*;
import com.example.carforum.services.CommentService;
import com.example.carforum.services.PostService;
import com.example.carforum.services.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelMapper {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    public ModelMapper(PostService postService,
                       UserService userService,
                       CommentService commentService){
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }
    //POST DTO
    public Post fromDtoCreate(PostDto postDto, User creator){

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setUser(creator);

        return post;
    }

    public Post fromDtoUpdate(int id, PostDto postDto){

        Post post = postService.getById(id);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        return post;
    }


    //TODO check if method works
    public PostDto toDtoUpdate(int id, Post post){

        PostDto dto = new PostDto();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());

        return dto;
    }

    //USER DTO
    public User fromDtoCreate(UserCreateDto userCreateDto){

        User user = new User();
        user.setUsername(userCreateDto.getUsername());
        user.setPassword(userCreateDto.getPassword());
        user.setFirstName(userCreateDto.getFirstName());
        user.setLastName(userCreateDto.getLastName());
        user.setEmail(userCreateDto.getEmail());
        user.setPhoneNumber(userCreateDto.getPhoneNumber());
        user.setAdmin(false);

        return user;
    }

    public User fromDtoUpdate(User user, UserUpdateDto userUpdateDto){

        user.setPassword(userUpdateDto.getPassword());
        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setEmail(userUpdateDto.getEmail());
        user.setPhoneNumber(userUpdateDto.getPhoneNumber());

        return user;
    }

    public UserUpdateDto toDtoUpdate(User user){
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPassword(user.getPassword());
        return dto;
    }


    //COMMENT DTO
    public Comment fromDtoCreate(CommentDto commentDto){

        Comment comment = new Comment();
        User user = userService.getById(commentDto.getUserId());
        Post post = postService.getById(commentDto.getPostId());

        comment.setContent(commentDto.getContent());
        comment.setUser(user);
        comment.setPost(post);

        return comment;
    }

    public Comment fromDtoUpdate(int id, CommentDto commentDto){

        Comment comment = commentService.getById(id);
        comment.setContent(commentDto.getContent());

        return comment;
    }

    //LIKE DTO

    public Like fromDtoCreate(int postId, User user){

        Like like = new Like();
        like.setPost(postService.getById(postId));
        like.setUser(user);

        return like;
    }

    public Like fromDtoDelete(int postId, User user){

        Like like = new Like();

        like.setPost(postService.getById(postId));
        like.setUser(user);

        return like;
    }

    //PostDETAILSDto

    public PostDetailsDto toDto(Post post, List<Comment> comments, int likes, boolean isLiked){
        PostDetailsDto dto = new PostDetailsDto();
        dto.setPost(post);
        dto.setComments(comments);
        dto.setLikes(likes);
        dto.setLiked(isLiked);

        return dto;
    }

    //LikeDto

    public Like fromDto(int postId, int userId){
        Like like = new Like();
        like.setPost(postService.getById(postId));
        like.setUser(userService.getById(userId));

        return like;
    }

}
