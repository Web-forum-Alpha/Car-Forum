package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import com.example.carforum.models.Like;
import com.example.carforum.models.User;

public interface LikeRepository {

    Like findIfPostLikedByUser(Post post, User user);


    void create(Like like);
    void delete(Like like);
}
