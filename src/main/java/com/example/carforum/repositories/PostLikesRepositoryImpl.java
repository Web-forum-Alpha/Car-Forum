package com.example.carforum.repositories;

import com.example.carforum.models.Post;
import com.example.carforum.models.PostLikes;
import com.example.carforum.models.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PostLikesRepositoryImpl implements PostLikesRepository{

    private final EntityManager entityManager;

    @Autowired
    public PostLikesRepositoryImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }



    @Override
    public PostLikes getLikeByUser(Post post, User user) {
        List<PostLikes> result = entityManager
                .createQuery(
                        "FROM PostLikes pl " +
                                "WHERE pl.post.id =:pId AND pl.user.id =:uId",PostLikes.class
                ).setParameter("pId", post.getId())
                .setParameter("uId", user.getId())
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<PostLikes> getAllLikesPerPost(Post post) {
        return   entityManager
                .createQuery(
                        "FROM PostLikes pl WHERE pl.post.id =:id", PostLikes.class
                ).setParameter("id", post.getId())
                .getResultList();


    }

    @Transactional
    @Override
    public void create(PostLikes postLikes) {

        entityManager.persist(postLikes);
    }

    @Transactional
    @Override
    public void delete(PostLikes postLikes) {

        entityManager.remove(postLikes);
    }
}
