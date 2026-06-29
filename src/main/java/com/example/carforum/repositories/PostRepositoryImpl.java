package com.example.carforum.repositories;

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
    public List<Post> getAll() {
        return entityManager
                .createQuery("FROM Post ", Post.class)
                .getResultList();
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
}
