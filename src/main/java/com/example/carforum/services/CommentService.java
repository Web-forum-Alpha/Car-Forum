package com.example.carforum.services;

import com.example.carforum.models.Comment;
import com.example.carforum.models.User;

import java.util.List;

public interface CommentService {

    List<Comment> getAll();

    Comment getById(int id);

    List<Comment> getByPostId(int id);

    List<Comment> getByUserId(int id);

    void create(Comment comment);
    void update(Comment comment, User user);
    void deleteById(int id, User user);
}
