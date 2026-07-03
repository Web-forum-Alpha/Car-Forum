package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.PostLikes;
import com.example.carforum.repositories.PostLikesRepository;
import com.example.carforum.repositories.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostLikesServiceImpl implements PostLikesService{

    public static final String NON_FOUND_MESSAGE = "%s with %s %s not found!";
    public static final String POST_ALREADY_LIKED_MESSAGE = "Post already liked!";
    public static final String UNLIKE_ERROR_MESSAGE = "Like was not found for this Post and User!";
    private final PostLikesRepository postLikesRepository;
    private final PostRepository postRepository;

    public PostLikesServiceImpl(PostLikesRepository postLikesRepository,
                                PostRepository postRepository){
        this.postLikesRepository = postLikesRepository;
        this.postRepository = postRepository;
    }


    //MVC interaction with button LIKE
    @Override
    public void interactionWithLikeButton(PostLikes postLike) {

        PostLikes likeCheck = postLikesRepository.findIfPostLikedByUser(postLike.getPost(), postLike.getUser());

        if (likeCheck == null) {
            create(postLike);
        } else {
            delete(likeCheck);
        }
    }

    @Override
    public List<PostLikes> getAllLikesPerPostId(int id) {

        if (postRepository.getById(id) != null){
            return postLikesRepository.getAllLikesPerPostId(id);
        }else {
            throw new EntityNotFoundException(String.format(NON_FOUND_MESSAGE, "Post", "id", id));
        }
    }

    @Override
    public void create(PostLikes like) {
        if (postLikesRepository.findIfPostLikedByUser(like.getPost(), like.getUser()) == null){
            postLikesRepository.create(like);
        }else {
            throw new EntityDuplicateException(POST_ALREADY_LIKED_MESSAGE);
        }
    }

    @Override
    public void delete(PostLikes like) {

        PostLikes likeCheck = postLikesRepository.findIfPostLikedByUser(like.getPost(), like.getUser());

        if ( likeCheck != null){
            postLikesRepository.delete(likeCheck);
        }else {
            throw new EntityNotFoundException(UNLIKE_ERROR_MESSAGE);
        }

    }
}
