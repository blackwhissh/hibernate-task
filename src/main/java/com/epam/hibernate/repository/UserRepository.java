package com.epam.hibernate.repository;

import com.epam.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import javax.naming.AuthenticationException;
import java.util.logging.Logger;

@Repository
public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());
    @PersistenceContext
    private EntityManager entityManager;

    public Boolean usernameExists(String username) {
        Long count = (Long) entityManager.createQuery("select count(u) from User u where u.username like :username")
                .setParameter("username", username)
                .getSingleResult();
        logger.info("Count - " + count);
        return count > 0;
    }
    public User save(User user){
        logger.info("User saved successfully");
        return entityManager.merge(user);
    }

    @Transactional
    public void authenticate(String username, String password) throws AuthenticationException {
        if (usernameExists(username)) {
            User user = (User) entityManager.createQuery("from User u where u.username = :username")
                    .setParameter("username", username)
                    .getSingleResult();
            if (user.getPassword().equals(password)) {
                logger.info("User authenticated successfully");
            } else {
                logger.warning("Wrong password");
                throw new AuthenticationException();
            }
        } else {
            logger.warning("User not found");
            throw new EntityNotFoundException();
        }
    }

    @Transactional
    public void changePassword(String newPassword, Long userId) {
        entityManager.createQuery("update User u set u.password = :newPassword where u.userId = :userId")
                .setParameter("newPassword", newPassword)
                .setParameter("userId", userId)
                .executeUpdate();
        logger.info("Password changed successfully");
    }

    @Transactional
    public void activateDeactivate(Boolean isActive, Long userId) {
        entityManager.createQuery("update User u set u.isActive = :active where u.userId = :userId")
                .setParameter("active", isActive)
                .setParameter("userId", userId)
                .executeUpdate();
        logger.info("User activated/deactivated successfully");
    }



}
