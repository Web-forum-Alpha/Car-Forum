package com.example.carforum.controllers;

import com.example.carforum.exceptions.EntityDuplicateException;
import com.example.carforum.exceptions.EntityNotFoundException;
import com.example.carforum.helpers.AuthenticationHelper;
import com.example.carforum.helpers.ModelMapper;
import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Like;
import com.example.carforum.models.Post;
import com.example.carforum.models.PostDto;
import com.example.carforum.models.User;
import com.example.carforum.services.LikeService;
import com.example.carforum.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostRestControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private LikeService likeService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PostRestController controller = new PostRestController(
                postService,
                modelMapper,
                authenticationHelper,
                likeService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAll_ShouldReturnPostsAndPassFilters() throws Exception {
        User user = createUser(1, "alex", false);
        Post post = createPost(10, "A sufficiently long title", "A sufficiently long post content for testing.", user);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(postService.getAll(any(FilterOptions.class))).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts")
                        .param("title", "engine")
                        .param("username", "alex")
                        .param("likes", "5")
                        .param("sortBy", "title")
                        .param("orderBy", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("A sufficiently long title"))
                .andExpect(jsonPath("$[0].username").value("alex"));

        ArgumentCaptor<FilterOptions> captor = ArgumentCaptor.forClass(FilterOptions.class);
        verify(postService).getAll(captor.capture());

        FilterOptions options = captor.getValue();
        assertEquals("engine", options.getTitle().orElseThrow());
        assertEquals("alex", options.getUsername().orElseThrow());
        assertEquals(5, options.getLikesCount().orElseThrow());
        assertEquals("title", options.getSortBy().orElseThrow());
        assertEquals("desc", options.getOrderBy().orElseThrow());
        assertTrue(options.getCommentsCount().isEmpty());
    }

    @Test
    void getById_ShouldReturnPost_WhenItExists() throws Exception {
        User user = createUser(1, "alex", false);
        Post post = createPost(10, "A sufficiently long title", "A sufficiently long post content for testing.", user);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(postService.getById(10)).thenReturn(post);

        mockMvc.perform(get("/api/posts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("alex"));
    }

    @Test
    void getById_ShouldReturnNotFound_WhenPostDoesNotExist() throws Exception {
        when(authenticationHelper.getCurrentUser(any())).thenReturn(createUser(1, "alex", false));
        when(postService.getById(99)).thenThrow(new EntityNotFoundException("Post not found"));

        mockMvc.perform(get("/api/posts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ShouldCreatePost_WhenUserIsNotBlocked() throws Exception {
        User user = createUser(1, "alex", false);
        Post mappedPost = createPost(0, "A sufficiently long title", "A sufficiently long post content for testing.", user);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(authenticationHelper.isBlocked(user)).thenReturn(false);
        when(modelMapper.fromDtoCreate(any(), eq(user))).thenReturn(mappedPost);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson()))
                .andExpect(status().isOk());

        verify(postService).create(mappedPost);
    }

    @Test
    void create_ShouldReturnForbidden_WhenUserIsBlocked() throws Exception {
        User blockedUser = createUser(1, "alex", true);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(blockedUser);
        when(authenticationHelper.isBlocked(blockedUser)).thenReturn(true);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson()))
                .andExpect(status().isForbidden());

        verify(modelMapper, never()).fromDtoCreate(any(), any());
        verify(postService, never()).create(any());
    }

    @Test
    void update_ShouldUpdatePost_WhenRequestIsValid() throws Exception {
        User user = createUser(1, "alex", false);
        Post mappedPost = createPost(10, "A sufficiently long title", "A sufficiently long post content for testing.", user);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(authenticationHelper.isBlocked(user)).thenReturn(false);
        when(modelMapper.fromDtoUpdate(eq(10), any(PostDto.class))).thenReturn(mappedPost);

        mockMvc.perform(put("/api/posts/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPostJson()))
                .andExpect(status().isOk());

        verify(postService).update(mappedPost, user);
    }

    @Test
    void delete_ShouldDeletePost_WhenUserIsNotBlocked() throws Exception {
        User user = createUser(1, "alex", false);
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(authenticationHelper.isBlocked(user)).thenReturn(false);

        mockMvc.perform(delete("/api/posts/10"))
                .andExpect(status().isOk());

        verify(postService).deleteById(10, user);
    }

    @Test
    void getPostLikesCount_ShouldReturnCount() throws Exception {
        when(likeService.getLikesCount(10)).thenReturn(7);

        mockMvc.perform(get("/api/posts/10/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void createLike_ShouldReturnConflict_WhenPostIsAlreadyLiked() throws Exception {
        User user = createUser(1, "alex", false);
        Like like = createLike(user, createPost(10, "A sufficiently long title", "A sufficiently long post content for testing.", user));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(modelMapper.fromDtoCreate(10, user)).thenReturn(like);
        doThrow(new EntityDuplicateException("Post already liked!"))
                .when(likeService).create(like);

        mockMvc.perform(post("/api/posts/10/likes"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteLike_ShouldReturnNotFound_WhenLikeDoesNotExist() throws Exception {
        User user = createUser(1, "alex", false);
        Like like = createLike(user, createPost(10, "A sufficiently long title", "A sufficiently long post content for testing.", user));
        when(authenticationHelper.getCurrentUser(any())).thenReturn(user);
        when(modelMapper.fromDtoDelete(10, user)).thenReturn(like);
        doThrow(new EntityNotFoundException("Like not found"))
                .when(likeService).delete(like);

        mockMvc.perform(delete("/api/posts/10/likes"))
                .andExpect(status().isNotFound());
    }

    private String validPostJson() {
        return """
                {
                  "title": "A sufficiently long title",
                  "content": "A sufficiently long post content for testing."
                }
                """;
    }

    private User createUser(int id, String username, boolean blocked) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setBlocked(blocked);
        return user;
    }

    private Post createPost(int id, String title, String content, User user) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setUser(user);
        return post;
    }

    private Like createLike(User user, Post post) {
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        return like;
    }
}
