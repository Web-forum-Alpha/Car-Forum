package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.PostLikes;
import com.example.carforum.models.User;
import com.example.carforum.services.PostLikesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class PostLikesRestController {

    private final PostLikesService service;
    private final ModelMapper mapper;
    private final AuthenticationHelper helper;

    @Autowired
    public PostLikesRestController(PostLikesService service, ModelMapper mapper, AuthenticationHelper helper){
        this.service = service;
        this.mapper = mapper;
        this.helper = helper;
    }

    @GetMapping("post/{id}")
    public List<PostLikes> getAllLikesPerPostId(@PathVariable int id){

        try{
            return service.getAllLikesPerPostId(id);

        }catch (EntityNotFoundException e){

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @PostMapping("/post/{id}")
    public void crate(@PathVariable int id, HttpSession session){

        try{
            User user = helper.getCurrentUser(session);
            PostLikes like = mapper.fromDtoCreate(id, user);
            service.create(like);
        }catch (EntityDuplicateException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

    }
    @DeleteMapping("/post/{id}")
    public void delete(@PathVariable int id, HttpSession session){

        try{
            User user = helper.getCurrentUser(session);
            PostLikes like = mapper.fromDtoDelete(id, user);
            service.delete(like);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }


}
