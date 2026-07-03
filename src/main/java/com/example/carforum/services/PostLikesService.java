package com.example.carforum.services;

import com.example.carforum.models.Post;
import com.example.carforum.models.PostLikes;
import com.example.carforum.models.User;

import java.util.List;

public interface PostLikesService {

    void interactionWithLikeButton(PostLikes like);

    List<PostLikes> getAllLikesPerPostId(int id);

    void create(PostLikes like);
    void delete(PostLikes like);
}
