package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Like;
import com.example.carforum.repositories.LikeRepository;
import com.example.carforum.repositories.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {

    public static final String NON_FOUND_MESSAGE = "%s with %s %s not found!";
    public static final String POST_ALREADY_LIKED_MESSAGE = "Post already liked!";
    public static final String UNLIKE_ERROR_MESSAGE = "Like was not found for this Post and User!";
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeServiceImpl(LikeRepository likeRepository,
                           PostRepository postRepository){
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }


    //MVC interaction with button LIKE
    @Override
    public void interactionWithLikeButton(Like postLike) {

        Like likeCheck = likeRepository.findIfPostLikedByUser(postLike.getPost(), postLike.getUser());

        if (likeCheck == null) {
            create(postLike);
        } else {
            delete(likeCheck);
        }
    }

    @Override
    public void create(Like like) {
        if (likeRepository.findIfPostLikedByUser(like.getPost(), like.getUser()) == null){
            likeRepository.create(like);
        }else {
            throw new EntityDuplicateException(POST_ALREADY_LIKED_MESSAGE);
        }
    }

    @Override
    public void delete(Like like) {

        Like likeCheck = likeRepository.findIfPostLikedByUser(like.getPost(), like.getUser());

        if ( likeCheck != null){
            likeRepository.delete(likeCheck);
        }else {
            throw new EntityNotFoundException(UNLIKE_ERROR_MESSAGE);
        }

    }
}
