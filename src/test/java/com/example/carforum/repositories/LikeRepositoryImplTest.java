package com.example.carforum.repositories;

import com.example.carforum.models.Like;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class LikeRepositoryImplTest {
    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Like> likeTypedQuery;

    @Mock
    private TypedQuery<Long> countTypedQuery;

    @InjectMocks
    private LikeRepositoryImpl likeRepository;

    private Like like;
    private Post post;
    private User user;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1);

        user = new User();
        user.setId(2);

        like = new Like();
        like.setPost(post);
        like.setUser(user);
    }

    @Test
    void create_ShouldCallPersistOnEntityManager() {
        likeRepository.create(like);

        verify(entityManager, times(1)).persist(like);
    }

    @Test
    void delete_ShouldFindLikeAndRemoveIt() {
        when(entityManager.createQuery(anyString(), eq(Like.class)))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.setParameter("postId", post.getId()))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.setParameter("userId", user.getId()))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.getSingleResult()).thenReturn(like);

        likeRepository.delete(like);

        verify(entityManager).createQuery(
                "SELECT l FROM Like l WHERE l.post.id =:postId AND l.user.id=:userId",
                Like.class);
        verify(likeTypedQuery).setParameter("postId", post.getId());
        verify(likeTypedQuery).setParameter("userId", user.getId());
        verify(entityManager, times(1)).remove(like);
    }

    @Test
    void delete_WhenLikeNotFound_ShouldPropagateNoResultException() {
        when(entityManager.createQuery(anyString(), eq(Like.class)))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.setParameter(anyString(), anyInt()))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.getSingleResult())
                .thenThrow(new NoResultException("No like found"));

        assertThrows(NoResultException.class, () -> likeRepository.delete(like));

        verify(entityManager, never()).remove(any());
    }

    @Test
    void existsByPostAndUser_WhenCountGreaterThanZero_ShouldReturnTrue() {
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(countTypedQuery);
        when(countTypedQuery.setParameter(anyString(), anyInt()))
                .thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(1L);

        boolean result = likeRepository.existsByPostAndUser(1, 2);

        assertTrue(result);
        verify(countTypedQuery).setParameter("postId", 1);
        verify(countTypedQuery).setParameter("userId", 2);
    }

    @Test
    void existsByPostAndUser_WhenCountIsZero_ShouldReturnFalse() {
        when(entityManager.createQuery(anyString(), eq(Long.class)))
                .thenReturn(countTypedQuery);
        when(countTypedQuery.setParameter(anyString(), anyInt()))
                .thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(0L);

        boolean result = likeRepository.existsByPostAndUser(1, 2);

        assertFalse(result);
    }

    @Test
    void countByPost_ShouldReturnSizeOfResultList() {
        Like anotherLike = new Like();
        anotherLike.setPost(post);
        anotherLike.setUser(new User());

        when(entityManager.createQuery(anyString(), eq(Like.class)))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.setParameter(anyString(), anyInt()))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.getResultList())
                .thenReturn(Arrays.asList(like, anotherLike));

        int count = likeRepository.countByPost(1);

        assertEquals(2, count);
        verify(likeTypedQuery).setParameter("postId", 1);
    }

    @Test
    void countByPost_WhenNoLikes_ShouldReturnZero() {
        when(entityManager.createQuery(anyString(), eq(Like.class)))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.setParameter(anyString(), anyInt()))
                .thenReturn(likeTypedQuery);
        when(likeTypedQuery.getResultList())
                .thenReturn(List.of());

        int count = likeRepository.countByPost(1);

        assertEquals(0, count);
    }

}
