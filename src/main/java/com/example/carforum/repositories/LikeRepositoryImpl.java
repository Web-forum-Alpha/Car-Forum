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


    @Override
    public Like findIfPostLikedByUser(Post post, User user) {
        List<Like> result = entityManager
                .createQuery(
                        "FROM Like pl " +
                                "WHERE pl.post.id =:pId AND pl.user.id =:uId", Like.class
                ).setParameter("pId", post.getId())
                .setParameter("uId", user.getId())
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }


    @Transactional
    @Override
    public void create(Like like) {

        entityManager.persist(like);
    }

    @Transactional
    @Override
    public void delete(Like like) {

        entityManager.remove(like);
    }
}
