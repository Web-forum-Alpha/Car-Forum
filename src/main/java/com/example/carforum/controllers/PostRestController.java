package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Post;
import com.example.carforum.models.PostDto;
import com.example.carforum.services.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostService postService;
    private final ModelMapper mapper;

    @Autowired
    public PostRestController(PostService postService, ModelMapper mapper){
        this.postService = postService;
        this.mapper = mapper;
    }

    @GetMapping()
    public List<Post> getAll(){

        return postService.getAll();
    }

    @GetMapping("/{id}")
    public Post getById(@PathVariable int id){

        try {
            return postService.getById(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping()
    public void create(@Valid @RequestBody PostDto postDto){

        Post post = mapper.fromDtoCreate(postDto);

        postService.create(post);

    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody PostDto postDto){

        try {
            Post post = mapper.fromDtoUpdate(id, postDto);

            postService.update(post);

        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id){

        try{
            postService.deleteById(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


}
