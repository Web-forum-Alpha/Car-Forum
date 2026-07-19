package com.example.carforum.repositories;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTests {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> userTypedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        when(entityManager.createQuery("from User", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.getResultList()).thenReturn(List.of(user));

        List<User> result = userRepository.getAll();

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void getAll_WhenNoUsers_ShouldReturnEmptyList() {
        when(entityManager.createQuery("from User", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.getResultList()).thenReturn(List.of());

        List<User> result = userRepository.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCountUsers_ShouldReturnCount() {
        when(entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(10L);

        long result = userRepository.getCountUsers();

        assertEquals(10L, result);
    }

    @Test
    void getById_ShouldReturnUser_WhenFound() {
        when(entityManager.find(User.class, 1)).thenReturn(user);

        User result = userRepository.getById(1);

        assertEquals(user, result);
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(entityManager.find(User.class, 99)).thenReturn(null);

        User result = userRepository.getById(99);

        assertNull(result);
    }

    @Test
    void getByUsername_ShouldReturnUser_WhenFound() {
        when(entityManager.createQuery("from User where username = :username", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("username", "daniel")).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenReturn(user);

        User result = userRepository.getByUsername("daniel");

        assertEquals(user, result);
    }

    @Test
    void getByUsername_ShouldReturnNull_WhenNoResultException() {
        when(entityManager.createQuery("from User where username = :username", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("username", "unknown")).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenThrow(new NoResultException());

        User result = userRepository.getByUsername("unknown");

        assertNull(result);
    }

    @Test
    void getByEmail_ShouldReturnUser_WhenFound() {
        when(entityManager.createQuery("from User where email = :email", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("email", "daniel@abv.bg")).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenReturn(user);

        User result = userRepository.getByEmail("daniel@abv.bg");

        assertEquals(user, result);
    }

    @Test
    void getByEmail_ShouldReturnNull_WhenNoResultException() {
        when(entityManager.createQuery("from User where email = :email", User.class))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter("email", "missing@abv.bg")).thenReturn(userTypedQuery);
        when(userTypedQuery.getSingleResult()).thenThrow(new NoResultException());

        User result = userRepository.getByEmail("missing@abv.bg");

        assertNull(result);
    }

    @Test
    void search_WithAllParametersProvided_ShouldWrapEachInLikePattern() {
        when(entityManager.createQuery(anyString(), eq(User.class)))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter(anyString(), any())).thenReturn(userTypedQuery);
        when(userTypedQuery.getResultList()).thenReturn(List.of(user));

        List<User> result = userRepository.search("dan", "abv", "Daniel");

        assertEquals(1, result.size());
        verify(userTypedQuery).setParameter("username", "%dan%");
        verify(userTypedQuery).setParameter("email", "%abv%");
        verify(userTypedQuery).setParameter("firstName", "%Daniel%");
    }

    @Test
    void search_WithAllParametersNull_ShouldSetNullParameters() {
        when(entityManager.createQuery(anyString(), eq(User.class)))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter(anyString(), any())).thenReturn(userTypedQuery);
        when(userTypedQuery.getResultList()).thenReturn(List.of(user));

        List<User> result = userRepository.search(null, null, null);

        assertEquals(1, result.size());
        verify(userTypedQuery).setParameter("username", null);
        verify(userTypedQuery).setParameter("email", null);
        verify(userTypedQuery).setParameter("firstName", null);
    }

    @Test
    void search_WhenNoResultException_ShouldReturnNull() {
        when(entityManager.createQuery(anyString(), eq(User.class)))
                .thenReturn(userTypedQuery);
        when(userTypedQuery.setParameter(anyString(), any())).thenReturn(userTypedQuery);
        when(userTypedQuery.getResultList()).thenThrow(new NoResultException());

        List<User> result = userRepository.search("dan", null, null);

        assertNull(result);
    }

    @Test
    void create_ShouldCallPersist() {
        userRepository.create(user);

        verify(entityManager, times(1)).persist(user);
    }

    @Test
    void update_ShouldCallMerge() {
        userRepository.update(user);

        verify(entityManager, times(1)).merge(user);
    }

    @Test
    void delete_ShouldCallRemove() {
        userRepository.delete(user);

        verify(entityManager, times(1)).remove(user);
    }
}