package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findTop10ByOrderByIdDesc(); // 10 most recent posts
}
