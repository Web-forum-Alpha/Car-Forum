package com.example.carforum.services;

import com.example.carforum.models.Post;
import com.example.carforum.models.User;

import java.util.List;

public interface PostService {

    Post getById(int id);

    List<Post> getAll();

    List<Post> getTenMostRecentPosts();
    List<Post> getTenMostCommentedPosts();

    void create(Post post);
    void update(Post post, User user);
    void deleteById(int id, User user);
}
