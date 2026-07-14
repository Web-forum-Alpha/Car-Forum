package com.example.carforum.repositories;

import com.example.carforum.models.Post;

import java.util.List;

public interface PostRepository{


    Post getById(int id);

    List<Post> getAll();

    List<Post> getTenMostRecentPosts();
    List<Post> getTenMostCommentedPosts();

    long getCountPosts();

    void create(Post post);
    void update(Post post);
    void delete(Post post);



}
