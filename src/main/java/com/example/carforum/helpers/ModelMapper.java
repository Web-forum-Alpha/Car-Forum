package com.example.carforum.helpers;

import com.example.carforum.models.Post;
import com.example.carforum.models.PostDto;
import com.example.carforum.models.User;
import com.example.carforum.models.UserDto;
import com.example.carforum.services.PostService;
import com.example.carforum.services.UserService;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {

    private static final int DEFAULT_LIKES_WHEN_POST_CREATED = 0;
    private final PostService postService;
    private final UserService userService;

    public ModelMapper(PostService postService, UserService userService){
        this.postService = postService;
        this.userService = userService;
    }
    //POST DTO
    public Post fromDtoCreate(PostDto postDto){

        Post post = new Post();
        User user = userService.getById(postDto.getUser_id());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setLikes(DEFAULT_LIKES_WHEN_POST_CREATED);
        post.setUser(user);

        return post;
    }

    public Post fromDtoUpdate(int id, PostDto postDto){

        Post post = postService.getById(id);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        return post;
    }

    //USER DTO
    public User fromDtoCreate(UserDto userDto){

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAdmin(false);

        return user;
    }

}
