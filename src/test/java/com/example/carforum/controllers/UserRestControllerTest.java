package com.example.carforum.controllers;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.User;
import com.example.carforum.models.UserCreateDto;
import com.example.carforum.models.UserUpdateDto;
import com.example.carforum.services.SupabaseStorageServiceImpl;
import com.example.carforum.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private SupabaseStorageServiceImpl supabaseStorageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserRestController controller = new UserRestController(
                userService,
                modelMapper,
                authenticationHelper,
                supabaseStorageService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAll_ShouldReturnUsers_WhenAccessIsAllowed() throws Exception {
        User user = createUser(1, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(userService.getAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("alex"));
    }

    @Test
    void getAll_ShouldReturnForbidden_WhenLoggedInUserIsNotAdmin() throws Exception {
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAll();
    }

    @Test
    void getById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(userService.getById(99)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_ShouldReturnBadRequest_WhenNoSearchParameterIsProvided() throws Exception {
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);

        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).search(any(), any(), any());
    }

    @Test
    void search_ShouldReturnMatchingUsers() throws Exception {
        User user = createUser(1, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(userService.search("alex", null, null)).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users/search").param("username", "alex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alex"));
    }

    @Test
    void search_ShouldReturnNotFound_WhenNoUserMatches() throws Exception {
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(userService.search("missing", null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/search").param("username", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_ShouldCreateUser_WhenUsernameAndEmailAreAvailable() throws Exception {
        User mappedUser = createUser(0, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(modelMapper.fromDtoCreate(any(UserCreateDto.class))).thenReturn(mappedUser);
        when(userService.search(null, "alex@example.com", null)).thenReturn(List.of());
        when(userService.search("alex", null, null)).thenReturn(List.of());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson()))
                .andExpect(status().isOk());

        verify(userService).create(mappedUser);
    }

    @Test
    void register_ShouldReturnForbidden_WhenEmailAlreadyExists() throws Exception {
        User mappedUser = createUser(0, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);
        when(modelMapper.fromDtoCreate(any(UserCreateDto.class))).thenReturn(mappedUser);
        when(userService.search(null, "alex@example.com", null)).thenReturn(List.of(new User()));
        when(userService.search("alex", null, null)).thenReturn(List.of());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson()))
                .andExpect(status().isForbidden());

        verify(userService, never()).create(any());
    }

    @Test
    void login_ShouldStoreUserInSession_WhenCredentialsAreValid() throws Exception {
        User user = createUser(1, "alex", "secret", "alex@example.com", false);
        MockHttpSession session = new MockHttpSession();
        when(authenticationHelper.isLoggedIn(any())).thenReturn(false);
        when(userService.getByUsername("alex")).thenReturn(user);

        mockMvc.perform(post("/api/users/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alex", "secret")))
                .andExpect(status().isOk());

        assertSame(user, session.getAttribute("currentUser"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenPasswordIsIncorrect() throws Exception {
        User user = createUser(1, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.isLoggedIn(any())).thenReturn(false);
        when(userService.getByUsername("alex")).thenReturn(user);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("alex", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_ShouldUpdateCurrentUserAndRefreshSession() throws Exception {
        User currentUser = createUser(1, "alex", "secret", "old@example.com", false);
        User updatedUser = createUser(1, "alex", "new-secret", "new@example.com", false);
        MockHttpSession session = new MockHttpSession();
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(userService.getById(1)).thenReturn(currentUser);
        when(modelMapper.fromDtoUpdate(eq(currentUser), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isOk());

        verify(userService).update(updatedUser);
        assertSame(updatedUser, session.getAttribute("currentUser"));
    }

    @Test
    void update_ShouldReturnForbidden_WhenUserTriesToUpdateAnotherAccount() throws Exception {
        User currentUser = createUser(1, "alex", "secret", "alex@example.com", false);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);

        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isForbidden());

        verify(userService, never()).update(any());
    }

    @Test
    void blockUser_ShouldDelegateToService() throws Exception {
        User admin = createUser(1, "admin", "secret", "admin@example.com", true);
        User target = createUser(2, "target", "secret", "target@example.com", false);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(admin);
        when(userService.getById(2)).thenReturn(target);

        mockMvc.perform(put("/api/users/2/block"))
                .andExpect(status().isOk());

        verify(userService).setBlock(target, admin, true);
    }

    @Test
    void uploadPicture_ShouldSavePathAndUpdateUser() throws Exception {
        User currentUser = createUser(1, "alex", "secret", "alex@example.com", false);
        MockMultipartFile picture = new MockMultipartFile(
                "picture",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );
        when(authenticationHelper.getCurrentUser(any())).thenReturn(currentUser);
        when(supabaseStorageService.uploadFile(any(), eq(1))).thenReturn("users/1/avatar.png");
        when(userService.getById(1)).thenReturn(currentUser);

        mockMvc.perform(multipart("/api/users/1/picture").file(picture))
                .andExpect(status().isOk());

        verify(userService).update(currentUser);
        org.junit.jupiter.api.Assertions.assertEquals(
                "users/1/avatar.png",
                currentUser.getProfilePicturePath()
        );
    }

    @Test
    void logout_ShouldReturnForbidden_WhenUserIsAlreadyLoggedOut() throws Exception {
        when(authenticationHelper.isLoggedIn(any())).thenReturn(false);

        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isForbidden());
    }

    private String validRegistrationJson() {
        return """
                {
                  "username": "alex",
                  "password": "secret",
                  "firstName": "Alexander",
                  "lastName": "Petrov",
                  "email": "alex@example.com",
                  "phoneNumber": "+359888123456"
                }
                """;
    }

    private String validUpdateJson() {
        return """
                {
                  "password": "new-secret",
                  "firstName": "Alexander",
                  "lastName": "Petrov",
                  "email": "new@example.com",
                  "phoneNumber": "+359888123456"
                }
                """;
    }

    private String loginJson(String username, String password) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }

    private User createUser(int id, String username, String password, String email, boolean admin) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setAdmin(admin);
        return user;
    }
}
