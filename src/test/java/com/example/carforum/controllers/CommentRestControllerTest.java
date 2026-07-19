package com.example.carforum.controllers;

import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.Comment;
import com.example.carforum.models.CommentDto;
import com.example.carforum.models.Post;
import com.example.carforum.models.User;
import com.example.carforum.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentRestControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CommentRestController controller = new CommentRestController(
                commentService,
                modelMapper,
                authenticationHelper
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAll_ShouldReturnComments() throws Exception {
        User user = createUser(1, "alex", false);
        Comment comment = createComment(10, "Useful comment", user, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(commentService.getAll()).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].content").value("Useful comment"))
                .andExpect(jsonPath("$[0].username").value("alex"))
                .andExpect(jsonPath("$[0].post_title").value("Engine discussion"));
    }

    @Test
    void getById_ShouldReturnComment() throws Exception {
        User user = createUser(1, "alex", false);
        Comment comment = createComment(10, "Useful comment", user, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(commentService.getById(10)).thenReturn(comment);

        mockMvc.perform(get("/api/comments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.content").value("Useful comment"));
    }

    @Test
    void getAllByUserId_ShouldReturnUsersComments() throws Exception {
        User user = createUser(1, "alex", false);
        Comment comment = createComment(10, "Useful comment", user, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(commentService.getByUserId(1)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alex"));
    }

    @Test
    void getAllByPostId_ShouldReturnPostsComments() throws Exception {
        User user = createUser(1, "alex", false);
        Comment comment = createComment(10, "Useful comment", user, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(commentService.getByPostId(5)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/post/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].post_title").value("Engine discussion"));
    }

    @Test
    void create_ShouldCreateMappedComment() throws Exception {
        User user = createUser(1, "alex", false);
        Comment mappedComment = createComment(0, "Useful comment", user, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(modelMapper.fromDtoCreate(any(CommentDto.class))).thenReturn(mappedComment);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCommentJson()))
                .andExpect(status().isOk());

        verify(commentService).create(mappedComment);
    }

    @Test
    void create_ShouldReturnBadRequest_WhenContentIsMissing() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"postId\":5}"))
                .andExpect(status().isBadRequest());

        verify(modelMapper, never()).fromDtoCreate(any(CommentDto.class));
        verify(commentService, never()).create(any());
    }

    @Test
    void update_ShouldUpdateComment_WhenUserIsOwner() throws Exception {
        User owner = createUser(1, "alex", false);
        Comment comment = createComment(10, "Updated comment", owner, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(owner);
        when(modelMapper.fromDtoUpdate(eq(10), any(CommentDto.class))).thenReturn(comment);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCommentJson()))
                .andExpect(status().isOk());

        verify(commentService).update(comment, owner);
    }

    @Test
    void update_ShouldReturnForbidden_WhenUserDoesNotOwnComment() throws Exception {
        User owner = createUser(1, "alex", false);
        User anotherUser = createUser(2, "maria", false);
        Comment comment = createComment(10, "Updated comment", owner, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(anotherUser);
        when(modelMapper.fromDtoUpdate(eq(10), any(CommentDto.class))).thenReturn(comment);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCommentJson()))
                .andExpect(status().isForbidden());

        verify(commentService, never()).update(any(), any());
    }

    @Test
    void update_ShouldAllowAdminToUpdateAnotherUsersComment() throws Exception {
        User owner = createUser(1, "alex", false);
        User admin = createUser(2, "admin", true);
        Comment comment = createComment(10, "Updated comment", owner, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(admin);
        when(modelMapper.fromDtoUpdate(eq(10), any(CommentDto.class))).thenReturn(comment);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(false);

        mockMvc.perform(put("/api/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCommentJson()))
                .andExpect(status().isOk());

        verify(commentService).update(comment, admin);
    }

    @Test
    void delete_ShouldDeleteComment_WhenUserIsOwner() throws Exception {
        User owner = createUser(1, "alex", false);
        Comment comment = createComment(10, "Useful comment", owner, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(owner);
        when(commentService.getById(10)).thenReturn(comment);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(true);

        mockMvc.perform(delete("/api/comments/10"))
                .andExpect(status().isOk());

        verify(commentService).deleteById(10, owner);
    }

    @Test
    void delete_ShouldReturnForbidden_WhenUserDoesNotOwnComment() throws Exception {
        User owner = createUser(1, "alex", false);
        User anotherUser = createUser(2, "maria", false);
        Comment comment = createComment(10, "Useful comment", owner, createPost(5, "Engine discussion"));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(anotherUser);
        when(commentService.getById(10)).thenReturn(comment);
        when(authenticationHelper.isLoggedInNonAdmin(any())).thenReturn(true);

        mockMvc.perform(delete("/api/comments/10"))
                .andExpect(status().isForbidden());

        verify(commentService, never()).deleteById(anyInt(), any());
    }

    private String validCommentJson() {
        return """
                {
                  "content": "Updated comment",
                  "userId": 1,
                  "postId": 5
                }
                """;
    }

    private User createUser(int id, String username, boolean admin) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAdmin(admin);
        return user;
    }

    private Post createPost(int id, String title) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        return post;
    }

    private Comment createComment(int id, String content, User user, Post post) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        return comment;
    }
}
