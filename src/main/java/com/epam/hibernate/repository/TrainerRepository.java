package com.epam.hibernate.repository;

import com.epam.hibernate.entity.Trainer;
import com.epam.hibernate.entity.Training;
import com.epam.hibernate.entity.TrainingType;
import com.epam.hibernate.entity.TrainingTypeEnum;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class TrainerRepository {
    private static final Logger logger = Logger.getLogger(TrainerRepository.class.getName());
    @PersistenceContext
    private EntityManager entityManager;
    @Transactional
    public Trainer save(Trainer trainer){
        logger.info("Trainer saved successfully");
        return entityManager.merge(trainer);
    }
    public Trainer selectByUsername(String username){
        Trainer trainer;
        try{
            trainer = (Trainer) entityManager.createQuery("select t from Trainer t join fetch t.user u where u.username like :username")
                    .setParameter("username", username)
                    .getSingleResult();
            logger.info("User found");
        }catch (RuntimeException e){
            logger.warning("User not found");
            throw new EntityNotFoundException("Wrong username - " + username);

        }
        return trainer;
    }
    @Transactional
    public void updateTrainer(TrainingType newSpecialization, Long trainerId){
        entityManager.createQuery("update Trainer t set t.specialization = :newSpec where trainerId = :trainerId")
                .setParameter("newSpec", newSpecialization)
                .setParameter("trainerId",trainerId)
                .executeUpdate();
        logger.info("Trainer specialization updated successfully");
    }
    @Transactional
    @SuppressWarnings("unchecked")
    public List<Training> getTrainingList(String trainerUsername, Date fromDate, Date toDate,
                                          String traineeName, TrainingTypeEnum trainingTypeName){
        List<Training> trainingList = entityManager.createQuery("select t from Training t " +
                        "join t.trainee tr " +
                        "join t.trainer trn " +
                        "join t.trainingType tt " +
                        "where trn.user.username = :trainerUsername " +
                        "and (:fromDate is null or t.trainingDate >= :fromDate) " +
                        "and (:toDate is null or t.trainingDate <= :toDate) " +
                        "and (:traineeName is null or tr.user.firstName = :traineeName) " +
                        "and (:trainingTypeName is null or tt.trainingTypeName = :trainingTypeName)")
                .setParameter("trainerUsername", trainerUsername)
                .setParameter("fromDate", fromDate, TemporalType.DATE)
                .setParameter("toDate", toDate, TemporalType.DATE)
                .setParameter("traineeName", traineeName)
                .setParameter("trainingTypeName", trainingTypeName)
                .getResultList();
        if(trainingList.isEmpty()){
            logger.warning("Query performed, but no result found");
        }else {
            logger.info("Trainings found successfully");
        }
        return trainingList;
    }
    @SuppressWarnings("unchecked")
    public List<Trainer> getAllTrainers(){
        return entityManager.createQuery("from Trainer t").getResultList();
    }


}
