package com.example.carforum.repositories;

import com.example.carforum.models.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentRepositoryImplTests {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Comment> commentTypedQuery;

    @InjectMocks
    private CommentRepositoryImpl commentRepository;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(1);
    }

    @Test
    void getAll_ShouldReturnAllComments() {
        when(entityManager.createQuery("FROM Comment ", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of(comment));

        List<Comment> result = commentRepository.getAll();

        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));
    }

    @Test
    void getAll_WhenNoComments_ShouldReturnEmptyList() {
        when(entityManager.createQuery("FROM Comment ", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of());

        List<Comment> result = commentRepository.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_ShouldReturnComment_WhenFound() {
        when(entityManager.find(Comment.class, 1)).thenReturn(comment);

        Comment result = commentRepository.getById(1);

        assertEquals(comment, result);
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(entityManager.find(Comment.class, 99)).thenReturn(null);

        Comment result = commentRepository.getById(99);

        assertNull(result);
    }

    @Test
    void getByPostId_ShouldSetPostIdParameterAndReturnComments() {
        when(entityManager.createQuery("FROM Comment c WHERE c.post.id =:id", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.setParameter("id", 5)).thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of(comment));

        List<Comment> result = commentRepository.getByPostId(5);

        assertEquals(1, result.size());
        verify(commentTypedQuery).setParameter("id", 5);
    }

    @Test
    void getByPostId_WhenNoCommentsForPost_ShouldReturnEmptyList() {
        when(entityManager.createQuery("FROM Comment c WHERE c.post.id =:id", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.setParameter("id", 5)).thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of());

        List<Comment> result = commentRepository.getByPostId(5);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByUserId_ShouldSetUserIdParameterAndReturnComments() {
        when(entityManager.createQuery("FROM Comment c WHERE c.user.id =:id", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.setParameter("id", 7)).thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of(comment));

        List<Comment> result = commentRepository.getByUserId(7);

        assertEquals(1, result.size());
        verify(commentTypedQuery).setParameter("id", 7);
    }

    @Test
    void getByUserId_WhenNoCommentsForUser_ShouldReturnEmptyList() {
        when(entityManager.createQuery("FROM Comment c WHERE c.user.id =:id", Comment.class))
                .thenReturn(commentTypedQuery);
        when(commentTypedQuery.setParameter("id", 7)).thenReturn(commentTypedQuery);
        when(commentTypedQuery.getResultList()).thenReturn(List.of());

        List<Comment> result = commentRepository.getByUserId(7);

        assertTrue(result.isEmpty());
    }

    @Test
    void create_ShouldCallPersist() {
        commentRepository.create(comment);

        verify(entityManager, times(1)).persist(comment);
    }

    @Test
    void update_ShouldCallMerge() {
        commentRepository.update(comment);

        verify(entityManager, times(1)).merge(comment);
    }

    @Test
    void delete_ShouldCallRemove() {
        commentRepository.delete(comment);

        verify(entityManager, times(1)).remove(comment);
    }
}