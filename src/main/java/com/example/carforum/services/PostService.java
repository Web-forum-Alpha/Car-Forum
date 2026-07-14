package com.example.carforum.services;

import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;

import java.util.List;

public interface PostService {

    Post getById(int id);

    List<Post> getAll(FilterOptions filterOptions);

    List<Post> getTenMostRecentPosts();
    List<Post> getTenMostCommentedPosts();

    long getCountPosts();

    void create(Post post);
    void update(Post post, User user);
    void deleteById(int id, User user);
}
