package com.example.carforum.repositories;

import com.example.carforum.models.User;
import jakarta.persistence.EntityManager;

import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
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
    public List<User> getAll() {
        return entityManager
                .createQuery("from User", User.class)
                .getResultList();
    }

    @Override
    public long getCountUsers() {
        return entityManager
                .createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
    }

    @Override
    public User getById(int id) {
        return entityManager
                .find(User.class, id);
    }

    @Override
    public User getByUsername(String username) {
        try {
            return entityManager
                    .createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User getByEmail(String email) {
        try {
            return entityManager
                    .createQuery("from User where email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<User> search(String username, String email, String firstName) {
        try {
            return entityManager
                    .createQuery("""
                    from User u
                    where (:username is null or u.username like :username)
                      and (:email is null or u.email like :email)
                      and (:firstName is null or u.firstName like :firstName)
                    """, User.class)
                    .setParameter("username", username == null ? null : "%" + username + "%")
                    .setParameter("email", email == null ? null : "%" + email + "%")
                    .setParameter("firstName", firstName == null ? null : "%" + firstName + "%")
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }


    @Transactional
    @Override
    public void create(User user) {
        entityManager.persist(user);
    }

    @Transactional
    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }

    @Transactional
    @Override
    public void update(User user) {
        entityManager.merge(user);
    }
}
