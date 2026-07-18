package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Like;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.LikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    void isLikedByUser_ShouldReturnRepositoryResult() {
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(true);

        boolean result = likeService.isLikedByUser(10, 20);

        assertTrue(result);
        verify(likeRepository).existsByPostAndUser(10, 20);
    }

    @Test
    void getLikesCount_ShouldReturnRepositoryResult() {
        when(likeRepository.countByPost(10)).thenReturn(7);

        int result = likeService.getLikesCount(10);

        assertEquals(7, result);
        verify(likeRepository).countByPost(10);
    }

    @Test
    void create_ShouldPersistLike_WhenPostIsNotAlreadyLiked() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(false);

        likeService.create(like);

        verify(likeRepository).create(like);
    }

    @Test
    void create_ShouldThrow_WhenPostIsAlreadyLiked() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(true);

        EntityDuplicateException exception = assertThrows(
                EntityDuplicateException.class,
                () -> likeService.create(like)
        );

        assertEquals(LikeServiceImpl.POST_ALREADY_LIKED_MESSAGE, exception.getMessage());
        verify(likeRepository, never()).create(any());
    }

    @Test
    void delete_ShouldDeleteLike_WhenItExists() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(true);

        likeService.delete(like);

        verify(likeRepository).delete(like);
    }

    @Test
    void delete_ShouldThrow_WhenLikeDoesNotExist() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> likeService.delete(like)
        );

        assertEquals(LikeServiceImpl.UNLIKE_ERROR_MESSAGE, exception.getMessage());
        verify(likeRepository, never()).delete(any());
    }

    @Test
    void interactionWithLikeButton_ShouldCreateLike_WhenItDoesNotExist() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(false);

        likeService.interactionWithLikeButton(like);

        verify(likeRepository).create(like);
        verify(likeRepository, never()).delete(any());
        verify(likeRepository, times(2)).existsByPostAndUser(10, 20);
    }

    @Test
    void interactionWithLikeButton_ShouldDeleteLike_WhenItExists() {
        Like like = createLike(10, 20);
        when(likeRepository.existsByPostAndUser(10, 20)).thenReturn(true);

        likeService.interactionWithLikeButton(like);

        verify(likeRepository).delete(like);
        verify(likeRepository, never()).create(any());
        verify(likeRepository, times(2)).existsByPostAndUser(10, 20);
    }

    private Like createLike(int postId, int userId) {
        Post post = new Post();
        post.setId(postId);

        User user = new User();
        user.setId(userId);

        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        return like;
    }
}
