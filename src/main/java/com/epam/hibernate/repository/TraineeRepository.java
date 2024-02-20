package com.epam.hibernate.repository;

import com.epam.hibernate.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TemporalType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class TraineeRepository {
    public static final Logger logger = Logger.getLogger(TrainerRepository.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Transactional
    public void save(Trainee trainee){
        entityManager.merge(trainee);
        logger.info("Trainee saved successfully");
    }

    public Trainee selectByUsername(String username){
        Trainee trainee;
        try{
            trainee = (Trainee) entityManager.createQuery("select t from Trainee t join fetch t.user u where u.username like :username")
                    .setParameter("username", username)
                    .getSingleResult();
            logger.info("User found");
        }catch (RuntimeException e){
            logger.warning("User not found");
            throw new EntityNotFoundException("Wrong username - " + username);

        }
        return trainee;
    }
    @Transactional
    public void updateTrainee(Date dob, String address){
        if(dob != null){
            entityManager.createQuery("update Trainee t set t.dob = :dob")
                    .setParameter("dob", dob)
                    .executeUpdate();
            logger.info("Trainee date of birth updated successfully");
        }
        if(address != null){
            entityManager.createQuery("update Trainee t set t.address = :address")
                    .setParameter("address", address)
                    .executeUpdate();
            logger.info("Trainee address updated successfully");
        }
        if (dob == null && address == null){
            throw new IllegalArgumentException("Both options can not be null");
        }
    }
    @Transactional
    public void deleteTrainee(String username){
        Trainee trainee = selectByUsername(username);
        entityManager.createQuery("delete Training t where t.trainee = :trainee")
                        .setParameter("trainee", trainee)
                        .executeUpdate();
        entityManager.createQuery("delete Trainee t where t.traineeId  = :traineeId")
                        .setParameter("traineeId", trainee.getTraineeId())
                        .executeUpdate();
        entityManager.createQuery("delete User u where u.userId = :userId")
                        .setParameter("userId", trainee.getUser().getUserId())
                        .executeUpdate();
        logger.info("Trainee removed successfully");
    }
    @Transactional
    @SuppressWarnings("unchecked")
    public List<Training> getTrainingList(String traineeUsername, Date fromDate, Date toDate,
                                          String trainerName, TrainingTypeEnum trainingTypeName){
        List<Training> trainingList = entityManager.createQuery("select t from Training t " +
                        "join t.trainee tr " +
                        "join t.trainer trn " +
                        "join t.trainingType tt " +
                        "where tr.user.username = :traineeUsername " +
                        "and (:fromDate is null or t.trainingDate >= :fromDate) " +
                        "and (:toDate is null or t.trainingDate <= :toDate) " +
                        "and (:trainerName is null or trn.user.firstName = :trainerName) " +
                        "and (:trainingTypeName is null or tt.trainingTypeName = :trainingTypeName)")
                .setParameter("traineeUsername", traineeUsername)
                .setParameter("fromDate", fromDate, TemporalType.DATE)
                .setParameter("toDate", toDate, TemporalType.DATE)
                .setParameter("trainerName", trainerName)
                .setParameter("trainingTypeName", trainingTypeName)
                .getResultList();
        if(trainingList.isEmpty()){
            logger.warning("Query performed, but no result found");
        }else {
            logger.info("Trainings found successfully");
        }
        return trainingList;
    }
}
