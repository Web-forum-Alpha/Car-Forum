package com.example.carforum.services;

import com.example.carforum.exceptions.AuthorizationException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.models.Comment;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.repositories.CommentRepository;
import com.example.carforum.repositories.PostRepository;
import com.example.carforum.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void getAll_ShouldReturnRepositoryResult() {
        List<Comment> expected = List.of(new Comment());
        when(commentRepository.getAll()).thenReturn(expected);

        List<Comment> actual = commentService.getAll();

        assertSame(expected, actual);
        verify(commentRepository).getAll();
    }

    @Test
    void getById_ShouldReturnComment_WhenItExists() {
        Comment expected = createComment(1, createUser(1, "owner", false, false));
        when(commentRepository.getById(1)).thenReturn(expected);

        Comment actual = commentService.getById(1);

        assertSame(expected, actual);
    }

    @Test
    void getById_ShouldThrow_WhenCommentDoesNotExist() {
        when(commentRepository.getById(42)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.getById(42)
        );

        assertEquals("Comment with 42 id was not found!", exception.getMessage());
    }

    @Test
    void getByPostId_ShouldReturnComments_WhenPostExists() {
        Post post = new Post();
        List<Comment> expected = List.of(new Comment());
        when(postRepository.getById(1)).thenReturn(post);
        when(commentRepository.getByPostId(1)).thenReturn(expected);

        List<Comment> actual = commentService.getByPostId(1);

        assertSame(expected, actual);
    }

    @Test
    void getByPostId_ShouldThrow_WhenPostDoesNotExist() {
        when(postRepository.getById(1)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.getByPostId(1)
        );

        assertEquals("Post with 1 id was not found!", exception.getMessage());
        verify(commentRepository, never()).getByPostId(anyInt());
    }

    @Test
    void getByUserId_ShouldReturnComments_WhenUserExists() {
        User user = createUser(1, "owner", false, false);
        List<Comment> expected = List.of(new Comment());
        when(userRepository.getById(1)).thenReturn(user);
        when(commentRepository.getByUserId(1)).thenReturn(expected);

        List<Comment> actual = commentService.getByUserId(1);

        assertSame(expected, actual);
    }

    @Test
    void getByUserId_ShouldThrow_WhenUserDoesNotExist() {
        when(userRepository.getById(1)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.getByUserId(1)
        );

        assertEquals("User with 1 id was not found!", exception.getMessage());
        verify(commentRepository, never()).getByUserId(anyInt());
    }

    @Test
    void create_ShouldPersistComment_WhenAuthorIsNotBlocked() {
        Comment comment = createComment(1, createUser(1, "owner", false, false));

        commentService.create(comment);

        verify(commentRepository).create(comment);
    }

    @Test
    void create_ShouldThrow_WhenAuthorIsBlocked() {
        Comment comment = createComment(1, createUser(1, "owner", false, true));

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> commentService.create(comment)
        );

        assertEquals("You are not authorized to create comments!", exception.getMessage());
        verify(commentRepository, never()).create(any());
    }

    @Test
    void update_ShouldPersistComment_WhenUserIsOwner() {
        User owner = createUser(1, "owner", false, false);
        Comment comment = createComment(1, owner);

        commentService.update(comment, owner);

        verify(commentRepository).update(comment);
    }

    @Test
    void update_ShouldPersistComment_WhenUserIsAdmin() {
        Comment comment = createComment(1, createUser(1, "owner", false, false));
        User admin = createUser(2, "admin", true, false);

        commentService.update(comment, admin);

        verify(commentRepository).update(comment);
    }

    @Test
    void update_ShouldThrow_WhenUserIsNeitherOwnerNorAdmin() {
        Comment comment = createComment(1, createUser(1, "owner", false, false));
        User anotherUser = createUser(2, "another", false, false);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> commentService.update(comment, anotherUser)
        );

        assertEquals(CommentServiceImpl.UPDATE_MESSAGE_COMMENT, exception.getMessage());
        verify(commentRepository, never()).update(any());
    }

    @Test
    void deleteById_ShouldDeleteComment_WhenUserIsOwner() {
        User owner = createUser(1, "owner", false, false);
        Comment comment = createComment(1, owner);
        when(commentRepository.getById(1)).thenReturn(comment);

        commentService.deleteById(1, owner);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteById_ShouldDeleteComment_WhenUserIsAdmin() {
        Comment comment = createComment(1, createUser(1, "owner", false, false));
        User admin = createUser(2, "admin", true, false);
        when(commentRepository.getById(1)).thenReturn(comment);

        commentService.deleteById(1, admin);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteById_ShouldThrow_WhenUserIsNeitherOwnerNorAdmin() {
        Comment comment = createComment(1, createUser(1, "owner", false, false));
        User anotherUser = createUser(2, "another", false, false);
        when(commentRepository.getById(1)).thenReturn(comment);

        AuthorizationException exception = assertThrows(
                AuthorizationException.class,
                () -> commentService.deleteById(1, anotherUser)
        );

        assertEquals(CommentServiceImpl.DELETE_MESSAGE_ERROR, exception.getMessage());
        verify(commentRepository, never()).delete(any());
    }

    private User createUser(int id, String username, boolean admin, boolean blocked) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAdmin(admin);
        user.setBlocked(blocked);
        return user;
    }

    private Comment createComment(int id, User author) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUser(author);
        return comment;
    }
}
