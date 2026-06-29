package com.example.carforum.services;

import com.example.carforum.models.Post;

import java.util.List;

public interface PostService {


    Post getById(int id);

    List<Post> getAll();

    List<Post> getTenMostRecentPosts();

    void create(Post post);
    void update(Post post);
    void deleteById(int id);
}
