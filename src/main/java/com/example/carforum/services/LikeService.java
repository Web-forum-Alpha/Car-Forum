package com.example.carforum.services;

import com.example.carforum.models.Like;

public interface LikeService {

    void interactionWithLikeButton(Like like);

    void create(Like like);
    void delete(Like like);
}
