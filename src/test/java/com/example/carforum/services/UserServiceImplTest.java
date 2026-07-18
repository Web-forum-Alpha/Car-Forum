package com.example.carforum.services;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.models.User;
import com.example.carforum.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAll_ShouldReturnRepositoryResult() {
        List<User> expected = List.of(createUser("alex", "alex@example.com", false));
        when(userRepository.getAll()).thenReturn(expected);

        List<User> actual = userService.getAll();

        assertSame(expected, actual);
        verify(userRepository).getAll();
    }

    @Test
    void create_ShouldPersistUser_WhenUsernameAndEmailAreAvailable() {
        User user = createUser("alex", "alex@example.com", false);
        when(userRepository.getByUsername(user.getUsername())).thenReturn(null);
        when(userRepository.getByEmail(user.getEmail())).thenReturn(null);

        userService.create(user);

        verify(userRepository).create(user);
    }

    @Test
    void create_ShouldThrow_WhenUsernameAlreadyExists() {
        User user = createUser("alex", "alex@example.com", false);
        when(userRepository.getByUsername(user.getUsername())).thenReturn(new User());

        EntityDuplicateException exception = assertThrows(
                EntityDuplicateException.class,
                () -> userService.create(user)
        );

        assertEquals("User with username alex already exists!", exception.getMessage());
        verify(userRepository, never()).getByEmail(anyString());
        verify(userRepository, never()).create(any());
    }

    @Test
    void create_ShouldThrow_WhenEmailAlreadyExists() {
        User user = createUser("alex", "alex@example.com", false);
        when(userRepository.getByUsername(user.getUsername())).thenReturn(null);
        when(userRepository.getByEmail(user.getEmail())).thenReturn(new User());

        EntityDuplicateException exception = assertThrows(
                EntityDuplicateException.class,
                () -> userService.create(user)
        );

        assertEquals("User with email alex@example.com already exists!", exception.getMessage());
        verify(userRepository, never()).create(any());
    }

    @Test
    void setBlock_ShouldBlockUser_WhenCurrentUserIsAdmin() {
        User target = createUser("target", "target@example.com", false);
        User admin = createUser("admin", "admin@example.com", true);

        userService.setBlock(target, admin, true);

        assertTrue(target.isBlocked());
        verify(userRepository).update(target);
    }

    @Test
    void setBlock_ShouldUnblockUser_WhenCurrentUserIsAdmin() {
        User target = createUser("target", "target@example.com", false);
        target.setBlocked(true);
        User admin = createUser("admin", "admin@example.com", true);

        userService.setBlock(target, admin, false);

        assertFalse(target.isBlocked());
        verify(userRepository).update(target);
    }

    @Test
    void setBlock_ShouldThrow_WhenCurrentUserIsNotAdmin() {
        User target = createUser("target", "target@example.com", false);
        User regularUser = createUser("regular", "regular@example.com", false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.setBlock(target, regularUser, true)
        );

        assertEquals(403, exception.getStatusCode().value());
        assertFalse(target.isBlocked());
        verify(userRepository, never()).update(any());
    }

    private User createUser(String username, String email, boolean admin) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setAdmin(admin);
        return user;
    }
}
