package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import com.example.carforum.models.Like;
import com.example.carforum.models.User;

public interface LikeRepository {

    void create(Like like);
    void delete(Like like);

    boolean existsByPostAndUser(int postId, int userId);
    int countByPost(int postId);

}
