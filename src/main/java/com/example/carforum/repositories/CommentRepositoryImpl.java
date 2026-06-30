package com.example.carforum.repositories;

import com.example.carforum.models.Comment;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class CommentRepositoryImpl implements CommentRepository{

    private final EntityManager entityManager;

    @Autowired
    public CommentRepositoryImpl(EntityManager entityManager){

        this.entityManager = entityManager;
    }

    @Override
    public List<Comment> getAll() {
        return entityManager
                .createQuery("FROM Comment ", Comment.class)
                .getResultList();
    }

    @Override
    public Comment getById(int id) {
        return entityManager.find(Comment.class, id);
    }

    @Override
    public List<Comment> getByPostId(int id) {
        return entityManager
                .createQuery(
                        "FROM Comment c WHERE c.post.id =:id"
                        , Comment.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Override
    public List<Comment> getByUserId(int id) {
        return entityManager
                .createQuery("FROM Comment c WHERE c.user.id =:id",
                        Comment.class)
                .setParameter("id", id)
                .getResultList();
    }

    @Transactional
    @Override
    public void create(Comment comment) {

        entityManager.persist(comment);
    }

    @Transactional
    @Override
    public void update(Comment comment) {

        entityManager.merge(comment);
    }

    @Transactional
    @Override
    public void delete(Comment comment) {

        entityManager.remove(comment);
    }
}
