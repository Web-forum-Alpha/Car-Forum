package com.example.carforum.services;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Like;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {


    private static final String NOT_FOUND_MESSAGE = "%s with %d id was not found!";
    public static final String OWNER_ERROR_MESSAGE = "You are not authorized to edit/delete posts created by other users.";
    private final PostRepository repository;

    @Autowired
    public PostServiceImpl(PostRepository repository) {
        this.repository = repository;
    }

    @Override
    public Post getById(int id) {
        Post post = repository.getById(id);

        if (post == null) {
            throw new EntityNotFoundException(String.format(NOT_FOUND_MESSAGE, "Post", id));
        } else {
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
    public void update(Post post, User user) {
        checkModifyPermission(post, user);

        //TODO ?
//        if (post.getUser().getId() != user.getId()) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, OWNER_ERROR_MESSAGE);
//        }
        repository.update(post);
    }

    @Override
    public void deleteById(int id, User user) {
        Post post = getById(id);
        if (post.getUser().getId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, OWNER_ERROR_MESSAGE);
        }
        repository.delete(post);
    }


    //TODO ASK Aleks
    private void checkModifyPermission(Post post, User user){;

        if (!(post.getUser().getUsername().equals(user.getUsername()) || user.isAdmin())){

            throw new AuthorizationException("Only admin or Post owner can modify!");
        }

    }
}
