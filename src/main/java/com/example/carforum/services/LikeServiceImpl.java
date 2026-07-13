package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Like;
import com.example.carforum.repositories.LikeRepository;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    public static final String POST_ALREADY_LIKED_MESSAGE = "Post already liked!";
    public static final String UNLIKE_ERROR_MESSAGE = "Like was not found for this Post and User!";
    private final LikeRepository likeRepository;

    public LikeServiceImpl(LikeRepository likeRepository){
        this.likeRepository = likeRepository;
    }


    //MVC interaction with button LIKE
    @Override
    public void interactionWithLikeButton(Like postLike) {

        if (!isLikedByUser(postLike.getPost().getId(), postLike.getUser().getId())) {
            create(postLike);

        } else {
            delete(postLike);

        }
    }

    @Override
    public boolean isLikedByUser(int postId, int userId) {

        return likeRepository.existsByPostAndUser(postId, userId);

    }

    @Override
    public int getLikesCount(int postId) {

        return likeRepository.countByPost(postId);
    }

    @Override
    public void create(Like like) {
        if (!isLikedByUser(like.getPost().getId(), like.getUser().getId())){
            likeRepository.create(like);
        }else {
            throw new EntityDuplicateException(POST_ALREADY_LIKED_MESSAGE);
        }
    }

    @Override
    public void delete(Like like) {

        if (isLikedByUser(like.getPost().getId(), like.getUser().getId())){
            likeRepository.delete(like);
        }else {
            throw new EntityNotFoundException(UNLIKE_ERROR_MESSAGE);
        }

    }
}
