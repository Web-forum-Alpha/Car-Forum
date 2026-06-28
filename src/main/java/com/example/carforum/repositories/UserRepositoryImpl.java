package com.example.carforum.repositories;

import com.example.carforum.models.User;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final EntityManager entityManager;

    @Autowired
    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<User> getAll(){
        return entityManager
                .createQuery("from User", User.class)
                .getResultList();
    }

    @Override
    public User getById(int id){
        return entityManager
                .find(User.class,id);
    }

    @Override
    public User getByUsername(String userName){
        return entityManager
                .createQuery("from User where userName = : username",User.class)
                .setParameter("username",userName)
                .getSingleResult();
    }
}
