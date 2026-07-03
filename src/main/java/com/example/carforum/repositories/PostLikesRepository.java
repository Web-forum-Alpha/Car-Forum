package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import com.example.carforum.models.PostLikes;
import com.example.carforum.models.User;

import java.util.List;

public interface PostLikesRepository {

    PostLikes findIfPostLikedByUser(Post post, User user);

    List<PostLikes> getAllLikesPerPostId(int id);

    void create(PostLikes postLikes);
    void delete(PostLikes postLikes);
}
