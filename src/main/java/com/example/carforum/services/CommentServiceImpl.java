package com.example.carforum.services;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Comment;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.CommentRepository;
import com.example.carforum.repositories.PostRepository;
import com.example.carforum.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private static final String NOT_FOUND_MESSAGE = "%s with %d id was not found!";
    private static final String BLOCKED_USER_MESSAGE = "You are not authorized to create comments!";
    public static final String DELETE_MESSAGE_ERROR = "You are not authorized to delete this comment!";
    public static final String UPDATE_MESSAGE_COMMENT = "You are not authorized to edit this comment!";

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository repository,
                              UserRepository userRepository,
                              PostRepository postRepository){
        this.commentRepository = repository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<Comment> getAll() {
        return commentRepository.getAll();
    }

    @Override
    public Comment getById(int id) {
        Comment comment = commentRepository.getById(id);
        if (comment == null){
            throw new EntityNotFoundException(
                    String.format(NOT_FOUND_MESSAGE, "Comment", id)
            );
        }
        return comment;
    }

    @Override
    public List<Comment> getByPostId(int id) {

        if (postRepository.getById(id) == null){
            throw new EntityNotFoundException(
                    String.format(NOT_FOUND_MESSAGE, "Post", id)
            );
        }
        return commentRepository.getByPostId(id);
    }

    @Override
    public List<Comment> getByUserId(int id) {
        if (userRepository.getById(id) == null){
            throw new EntityNotFoundException(
                    String.format(NOT_FOUND_MESSAGE, "User", id)
            );
        }
        return commentRepository.getByUserId(id);
    }

    @Transactional
    @Override
    public void create(Comment comment) {

        if (comment.getUser().isBlocked()){
            throw new AuthorizationException(BLOCKED_USER_MESSAGE);
        }

        commentRepository.create(comment);

    }

    @Override
    public void update(Comment comment, User user) {

        checkModifyPermission(comment, user, UPDATE_MESSAGE_COMMENT);
        commentRepository.update(comment);

    }

    @Override
    public void deleteById(int id, User user) {

        Comment comment = getById(id);

        checkModifyPermission(comment, user, DELETE_MESSAGE_ERROR);

        commentRepository.delete(comment);

    }

    private void checkModifyPermission(Comment comment, User user, String errorToThrow) {

        if (!(comment.getUser().getUsername().equals(user.getUsername()) || user.isAdmin())) {

            throw new AuthorizationException(errorToThrow);
        }

    }
}
