package com.example.carforum.services;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {


    private static final String NOT_FOUND_MESSAGE = "%s with %d id was not found!";
    private static final String DELETE_ERROR_MESSAGE = "You are not authorized to delete posts created by other users.";
    private static final String UPDATE_ERROR_MESSAGE = "You are not authorized to edit posts created by other users.";
    private static final String BLOCKED_USER_MESSAGE = "You are not authorized to create posts!";
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
    public List<Post> getAll(FilterOptions filterOptions) {
        return repository.getAll(filterOptions);
    }

    @Override
    public long getCountPosts() {

        return repository.getCountPosts();
    }

    @Override
    public List<Post> getTenMostRecentPosts() {
        return repository.getTenMostRecentPosts();
    }

    @Override
    public List<Post> getTenMostCommentedPosts() {
        return repository.getTenMostCommentedPosts();
    }

    @Override
    public void create(Post post) {


        if (post.getUser().isBlocked()){
            throw  new AuthorizationException(BLOCKED_USER_MESSAGE);
        }

        repository.create(post);
    }

    @Override
    public void update(Post post, User user) {


        checkModifyPermission(post, user, UPDATE_ERROR_MESSAGE);
        repository.update(post);
    }

    @Override
    public void deleteById(int id, User user) {

        Post post = getById(id);
        checkModifyPermission(post, user, DELETE_ERROR_MESSAGE);
        repository.delete(post);
    }

    private void checkModifyPermission(Post post, User user, String errorToThrow) {

        if (!(post.getUser().getUsername().equals(user.getUsername()) || user.isAdmin())) {

            throw new AuthorizationException(errorToThrow);
        }

    }
}
