package com.example.carforum.repositories;

import com.example.carforum.models.Comment;

import java.util.List;

public interface CommentRepository {

    List<Comment> getAll();

    Comment getById(int id);

    List<Comment> getByPostId(int id);

    List<Comment> getByUserId(int id);

    void create(Comment comment);
    void update(Comment comment);
    void delete(Comment comment);
}
