package com.example.carforum.services;

import com.example.carforum.models.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> getAll();

    Comment getById(int id);

    List<Comment> getByPostId(int id);

    List<Comment> getByUserId(int id);

    void create(Comment comment);
    void update(Comment comment);
    void deleteById(int id);
}
