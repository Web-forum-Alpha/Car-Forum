package com.example.carforum.services;

import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Post;
import com.example.carforum.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService{


    private static final String NOT_FOUND_MESSAGE = "%s with %d id was not found!";
    PostRepository repository;

    @Autowired
    public PostServiceImpl(PostRepository repository){
        this.repository = repository;
    }

    @Override
    public Post getById(int id) {

        Post post = repository.getById(id);

        if (post == null){

            throw new EntityNotFoundException(String.format(NOT_FOUND_MESSAGE,  "Post", id));
        }else {
            return post;
        }
    }

    @Override
    public List<Post> getAll() {
        return repository.getAll();
    }

    @Override
    public List<Post> getTenMostRecentPosts() {
        return repository.getTenMostRecentPosts();
    }

    @Override
    public void create(Post post) {

        repository.create(post);

    }

    @Override
    public void update(Post post) {

        repository.update(post);
    }

    @Override
    public void deleteById(int id) {

        Post post = getById(id);

        repository.delete(post);
    }
}
