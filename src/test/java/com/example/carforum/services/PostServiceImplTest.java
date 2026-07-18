package com.example.carforum.services;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void getById_ShouldReturnPost_WhenItExists() {
        Post expected = createPost(1, createUser("owner", false, false));
        when(postRepository.getById(1)).thenReturn(expected);

        Post actual = postService.getById(1);

        assertSame(expected, actual);
    }

    @Test
    void getById_ShouldThrow_WhenPostDoesNotExist() {
        when(postRepository.getById(42)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> postService.getById(42)
        );

        assertEquals("Post with 42 id was not found!", exception.getMessage());
    }

    @Test
    void create_ShouldPersistPost_WhenAuthorIsNotBlocked() {
        Post post = createPost(1, createUser("owner", false, false));

        postService.create(post);

        verify(postRepository).create(post);
    }

    @Test
    void create_ShouldThrow_WhenAuthorIsBlocked() {
        Post post = createPost(1, createUser("owner", false, true));

        assertThrows(AuthorizationException.class, () -> postService.create(post));

        verify(postRepository, never()).create(any());
    }

    @Test
    void update_ShouldPersistPost_WhenUserIsOwner() {
        User owner = createUser("owner", false, false);
        Post post = createPost(1, owner);

        postService.update(post, owner);

        verify(postRepository).update(post);
    }

    @Test
    void update_ShouldPersistPost_WhenUserIsAdmin() {
        Post post = createPost(1, createUser("owner", false, false));
        User admin = createUser("admin", true, false);

        postService.update(post, admin);

        verify(postRepository).update(post);
    }

    @Test
    void update_ShouldThrow_WhenUserIsNeitherOwnerNorAdmin() {
        Post post = createPost(1, createUser("owner", false, false));
        User anotherUser = createUser("another", false, false);

        assertThrows(AuthorizationException.class, () -> postService.update(post, anotherUser));

        verify(postRepository, never()).update(any());
    }

    @Test
    void deleteById_ShouldDeletePost_WhenUserIsOwner() {
        User owner = createUser("owner", false, false);
        Post post = createPost(1, owner);
        when(postRepository.getById(1)).thenReturn(post);

        postService.deleteById(1, owner);

        verify(postRepository).delete(post);
    }

    @Test
    void deleteById_ShouldThrow_WhenPostDoesNotExist() {
        User owner = createUser("owner", false, false);
        when(postRepository.getById(1)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> postService.deleteById(1, owner));

        verify(postRepository, never()).delete(any());
    }

    private User createUser(String username, boolean admin, boolean blocked) {
        User user = new User();
        user.setUsername(username);
        user.setAdmin(admin);
        user.setBlocked(blocked);
        return user;
    }

    private Post createPost(int id, User author) {
        Post post = new Post();
        post.setId(id);
        post.setUser(author);
        return post;
    }
}
