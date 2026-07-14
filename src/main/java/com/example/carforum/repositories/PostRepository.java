package com.example.carforum.repositories;

import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Post;

import java.util.List;

public interface PostRepository{


    Post getById(int id);

    List<Post> getAll(FilterOptions filterOptions);

    List<Post> getTenMostRecentPosts();
    List<Post> getTenMostCommentedPosts();

    long getCountPosts();

    void create(Post post);
    void update(Post post);
    void delete(Post post);



}
