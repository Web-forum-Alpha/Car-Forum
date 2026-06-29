package com.example.carforum.repositories;

import com.example.carforum.models.Post;

import java.util.List;

public interface PostRepository{


    Post getById(int id);

    List<Post> getAll();

    List<Post> getTenMostRecentPosts();

    void create(Post post);
    void update(Post post);
    void delete(Post post);



}
