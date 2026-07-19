package com.example.carforum.repositories;

import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostRepositoryImplTests {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Post> postTypedQuery;

    @Mock
    private TypedQuery<Long> longTypedQuery;

    @InjectMocks
    private PostRepositoryImpl postRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1);
    }

    @Test
    void getById_ShouldReturnPost_WhenFound() {
        when(entityManager.find(Post.class, 1)).thenReturn(post);

        Post result = postRepository.getById(1);

        assertEquals(post, result);
        verify(entityManager).find(Post.class, 1);
    }

    @Test
    void getById_ShouldReturnNull_WhenNotFound() {
        when(entityManager.find(Post.class, 99)).thenReturn(null);

        Post result = postRepository.getById(99);

        assertNull(result);
    }

    @Test
    void getAll_WithNoFilters_ShouldBuildBaseQueryAndReturnResults() {
        FilterOptions filterOptions = emptyFilterOptions();

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        List<Post> result = postRepository.getAll(filterOptions);

        assertEquals(1, result.size());
        verify(postTypedQuery, never()).setParameter(eq("title"), any());
        verify(postTypedQuery, never()).setParameter(eq("username"), any());
        verify(postTypedQuery, never()).setParameter(eq("likesCount"), any());
        verify(postTypedQuery, never()).setParameter(eq("commentsCount"), any());
    }

    @Test
    void getAll_WithTitleFilter_ShouldSetTitleParameter() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getTitle()).thenReturn(Optional.of("bmw"));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(postTypedQuery).setParameter("title", "bmw");
    }

    @Test
    void getAll_WithUsernameFilter_ShouldSetUsernameParameter() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getUsername()).thenReturn(Optional.of("daniel"));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(postTypedQuery).setParameter("username", "daniel");
    }

    @Test
    void getAll_WithLikesCountFilter_ShouldSetLikesCountAsLong() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getLikesCount()).thenReturn(Optional.of(5));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(postTypedQuery).setParameter("likesCount", 5L);
    }

    @Test
    void getAll_WithLikesAndCommentsCount_ShouldSetBothParameters() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getLikesCount()).thenReturn(Optional.of(3));
        when(filterOptions.getCommentsCount()).thenReturn(Optional.of(2));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(postTypedQuery).setParameter("likesCount", 3L);
        verify(postTypedQuery).setParameter("commentsCount", 2L);
    }

    @Test
    void getAll_WithOnlyCommentsCount_ShouldSetCommentsCountParameter() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getCommentsCount()).thenReturn(Optional.of(4));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(postTypedQuery).setParameter("commentsCount", 4L);
    }

    @Test
    void getAll_WithSortByLikesAscending_ShouldBuildQueryWithoutError() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getSortBy()).thenReturn(Optional.of("likes"));
        when(filterOptions.getOrderBy()).thenReturn(Optional.of("asc"));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        List<Post> result = postRepository.getAll(filterOptions);

        assertEquals(1, result.size());
        verify(entityManager).createQuery(contains("ORDER BY COUNT(DISTINCT l) ASC"), eq(Post.class));
    }

    @Test
    void getAll_WithSortByCommentsDescendingDefault_ShouldBuildQueryWithCorrectOrder() {
        FilterOptions filterOptions = emptyFilterOptions();
        when(filterOptions.getSortBy()).thenReturn(Optional.of("comments"));

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        postRepository.getAll(filterOptions);

        verify(entityManager).createQuery(contains("ORDER BY COUNT(DISTINCT c) DESC"), eq(Post.class));
    }

    @Test
    void getAll_WhenNoResults_ShouldReturnEmptyList() {
        FilterOptions filterOptions = emptyFilterOptions();

        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of());

        List<Post> result = postRepository.getAll(filterOptions);

        assertTrue(result.isEmpty());
    }

    private FilterOptions emptyFilterOptions() {
        FilterOptions filterOptions = mock(FilterOptions.class);
        lenient().when(filterOptions.getTitle()).thenReturn(Optional.empty());
        lenient().when(filterOptions.getUsername()).thenReturn(Optional.empty());
        lenient().when(filterOptions.getLikesCount()).thenReturn(Optional.empty());
        lenient().when(filterOptions.getCommentsCount()).thenReturn(Optional.empty());
        lenient().when(filterOptions.getSortBy()).thenReturn(Optional.empty());
        lenient().when(filterOptions.getOrderBy()).thenReturn(Optional.empty());
        return filterOptions;
    }

    @Test
    void getCountPosts_ShouldReturnCountFromEntityManager() {
        when(entityManager.createQuery("SELECT COUNT(p) FROM Post p", Long.class))
                .thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(42L);

        long result = postRepository.getCountPosts();

        assertEquals(42L, result);
    }

    @Test
    void create_ShouldCallPersist() {
        postRepository.create(post);

        verify(entityManager, times(1)).persist(post);
    }

    @Test
    void update_ShouldCallMerge() {
        postRepository.update(post);

        verify(entityManager, times(1)).merge(post);
    }

    @Test
    void delete_ShouldCallRemove() {
        postRepository.delete(post);

        verify(entityManager, times(1)).remove(post);
    }

    @Test
    void getTenMostRecentPosts_ShouldSetMaxResultsToTenAndReturnList() {
        when(entityManager.createQuery("FROM Post order by id DESC", Post.class))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.setMaxResults(10)).thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        List<Post> result = postRepository.getTenMostRecentPosts();

        assertEquals(1, result.size());
        verify(postTypedQuery).setMaxResults(10);
    }

    @Test
    void getTenMostCommentedPosts_ShouldSetMaxResultsToTenAndReturnList() {
        when(entityManager.createQuery(anyString(), eq(Post.class)))
                .thenReturn(postTypedQuery);
        when(postTypedQuery.setMaxResults(10)).thenReturn(postTypedQuery);
        when(postTypedQuery.getResultList()).thenReturn(List.of(post));

        List<Post> result = postRepository.getTenMostCommentedPosts();

        assertEquals(1, result.size());
        verify(postTypedQuery).setMaxResults(10);
        verify(entityManager).createQuery(contains("ORDER BY COUNT(c) DESC"), eq(Post.class));
    }
}
