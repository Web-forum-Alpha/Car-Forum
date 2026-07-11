package com.example.carforum.services;

import com.example.carforum.models.Like;

public interface LikeService {

    void interactionWithLikeButton(Like like);

    boolean isLikedByUser(int postId, int userId);

    int getLikesCount(int postId);

    void create(Like like);
    void delete(Like like);
}
