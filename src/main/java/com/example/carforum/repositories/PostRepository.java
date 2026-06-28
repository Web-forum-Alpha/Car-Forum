package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository{


    Post getPostById(int id);

    List<Post> getAllPosts();

    List<Post> getTenMostRecentPosts();

    void createPost(Post post);
    void updatePost(Post post);
    void deletePost(Post post);



}
