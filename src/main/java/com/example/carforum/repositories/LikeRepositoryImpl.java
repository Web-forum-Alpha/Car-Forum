package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import com.example.carforum.models.Like;
import com.example.carforum.models.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class LikeRepositoryImpl implements LikeRepository {

    private final EntityManager entityManager;

    @Autowired
    public LikeRepositoryImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public void create(Like like) {

        entityManager.persist(like);
    }

    @Transactional
    @Override
    public void delete(Like like) {

        Like manageLike = entityManager.createQuery("SELECT l FROM Like l WHERE l.post.id =:postId AND l.user.id=:userId", Like.class)
                .setParameter("postId", like.getPost().getId())
                .setParameter("userId", like.getUser().getId())
                .getSingleResult();

        entityManager.remove(manageLike);
    }

    @Override
    public boolean existsByPostAndUser(int postId, int userId) {

        Long count = entityManager
                .createQuery(
                        "SELECT COUNT(l) " +
                                "FROM Like l " +
                                "WHERE l.post.id = :postId AND l.user.id = :userId", Long.class
                ).setParameter("postId", postId)
                .setParameter("userId", userId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public int countByPost(int postId) {

        List<Like> likes = entityManager.createQuery("FROM Like l WHERE l.post.id =:postId", Like.class)
                .setParameter("postId", postId)
                .getResultList();

        return likes.size();
    }
}
