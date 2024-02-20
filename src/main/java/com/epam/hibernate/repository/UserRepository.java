package com.epam.hibernate.repository;

import com.epam.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class UserRepository {
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @SuppressWarnings("unchecked")
    public List<String> getAllUsername(){
        return entityManager.createQuery("select u.username from User u").getResultList();
    }
    public User getUser(Integer id){
       User user = entityManager.find(User.class, id);
       if(user != null){
           logger.info("User found");
       }else {
           logger.warning("User not found");
           throw new EntityNotFoundException("ID - " + id);
       }
       return user;
    }
    public Boolean usernameExists(String username){
        Long count = (Long) entityManager.createQuery("select count(u) from User u where u.username like :username")
                .setParameter("username", username)
                .getSingleResult();
        logger.info("Count - " + count);
        return count > 0;
    }
    @Transactional
    public void authenticate(String username, String password) throws AuthenticationException {
        if(usernameExists(username)){
            User user = (User) entityManager.createQuery("from User u where u.username = :username")
                    .setParameter("username", username)
                    .getSingleResult();
            if(user.getPassword().equals(password)){
                logger.info("User authenticated successfully");
            }else {
                logger.warning("Wrong password");
                throw new AuthenticationException();
            }
        }else {
            logger.warning("User not found");
            throw new EntityNotFoundException();
        }
    }
    @Transactional
    public void changePassword(String newPassword, Long userId){
        entityManager.createQuery("update User u set u.password = :newPassword where u.userId = :userId")
                .setParameter("newPassword", newPassword)
                .setParameter("userId", userId)
                .executeUpdate();
        logger.info("Password changed successfully");
    }
    @Transactional
    public void activateDeactivate(Boolean isActive, Long userId){
        entityManager.createQuery("update User u set u.isActive = :active where u.userId = :userId")
                .setParameter("active", isActive)
                .setParameter("userId", userId)
                .executeUpdate();
        logger.info("User activated/deactivated successfully");
    }


}
