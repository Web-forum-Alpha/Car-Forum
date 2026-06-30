package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Comment;
import com.example.carforum.models.CommentDto;
import com.example.carforum.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentRestController {

    private final CommentService service;
    private final ModelMapper mapper;


    public CommentRestController(CommentService service, ModelMapper mapper){
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping()
    public List<Comment> getAll(){

        return service.getAll();
    }

    @GetMapping("/{id}")
    public Comment getById(@PathVariable int id){

        try {
            return service.getById(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public List<Comment> getAllByUserId(@PathVariable int id){

        try{
            return service.getByUserId(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    @GetMapping("/post/{id}")
    public List<Comment> getAllByPostId(@PathVariable int id){

        try{
            return service.getByPostId(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping()
    public void create(@Valid @RequestBody CommentDto commentDto){

        Comment comment = mapper.fromDtoCreate(commentDto);

        service.create(comment);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable int id, @Valid @RequestBody CommentDto commentDto){

        try {
            Comment comment = mapper.fromDtoUpdate(id, commentDto);

            service.update(comment);

        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id){

        try{
            service.deleteById(id);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
