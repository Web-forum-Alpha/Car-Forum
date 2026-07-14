package com.example.carforum.repositories;

import com.example.carforum.models.FilterOptions;
import com.example.carforum.models.Post;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final EntityManager entityManager;

    @Autowired
    public PostRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Post getById(int id) {

        return entityManager
                .find(Post.class, id);
    }

    @Override
    public List<Post> getAll(FilterOptions filterOptions) {

        StringBuilder query = new StringBuilder("""
                SELECT p FROM Post p
                LEFT JOIN Like l ON l.post = p
                LEFT JOIN Comment c ON c.post = p WHERE 1=1
                """);

        filterOptions.getTitle().ifPresent(
                value -> query.append(" AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))"));

        filterOptions.getUsername().ifPresent(
                value -> query.append(" AND p.user.username = :username"));

        query.append(" GROUP BY p");

        filterOptions.getLikesCount().ifPresent(
                value -> query.append(" HAVING COUNT(DISTINCT l) >= :likesCount"));

        filterOptions.getCommentsCount().ifPresent(
                value ->{
                    if (filterOptions.getLikesCount().isPresent()) {
                        query.append(" AND COUNT(DISTINCT c) >= :commentsCount");
                    }else {
                        query.append(" HAVING COUNT(DISTINCT c) >= :commentsCount");
                    }
                }
        );

        query.append(buildOrderBy(filterOptions));

        var queryExecute = entityManager.createQuery(query.toString(), Post.class);

        filterOptions.getTitle().ifPresent(v -> queryExecute.setParameter("title", v));
        filterOptions.getUsername().ifPresent(v -> queryExecute.setParameter("username", v));
        filterOptions.getLikesCount().ifPresent(v -> queryExecute.setParameter("likesCount", v.longValue()));
        filterOptions.getCommentsCount().ifPresent(v -> queryExecute.setParameter("commentsCount", v.longValue()));

        return queryExecute.getResultList();

    }

    @Override
    public long getCountPosts() {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Post p", Long.class)
                .getSingleResult();
    }

    @Transactional
    @Override
    public void create(Post post) {

        entityManager.persist(post);
    }

    @Transactional
    @Override
    public void update(Post post) {

        entityManager.merge(post);
    }

    @Transactional
    @Override
    public void delete(Post post) {
        entityManager.remove(post);

    }

    @Override
    public List<Post> getTenMostRecentPosts() {
        return entityManager
                .createQuery("FROM Post order by id DESC", Post.class)
                .setMaxResults(10)
                .getResultList();
    }

    @Override
    public List<Post> getTenMostCommentedPosts() {
        return entityManager.createQuery(
                        "SELECT p FROM Post p " +
                                "LEFT JOIN Comment c ON c.post = p " +
                                "GROUP BY p " +
                                "ORDER BY COUNT(c) DESC", Post.class
                )
                .setMaxResults(10)
                .getResultList();
    }

    private String buildOrderBy(FilterOptions filterOptions) {
        String sortBy = filterOptions.getSortBy().orElse("id");
        String orderBy = filterOptions.getOrderBy().orElse("desc");

        String column = switch (sortBy) {
            case "title" -> "p.title";
            case "likes" -> "COUNT(DISTINCT l)";
            case "comments" -> "COUNT(DISTINCT c)";
            default -> "p.id";
        };

        String direction = orderBy.equalsIgnoreCase("asc") ? "ASC" : "DESC";

        return " ORDER BY " + column + " " + direction;
    }
}
