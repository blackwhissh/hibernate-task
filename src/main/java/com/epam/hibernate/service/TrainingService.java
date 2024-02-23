package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.*;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.io.NotActiveException;
import java.nio.file.AccessDeniedException;
import java.util.Date;

import static com.epam.hibernate.Utils.checkAdmin;

@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrainingService(TrainingRepository trainingRepository,
                           TrainerRepository trainerRepository,
                           TraineeRepository traineeRepository,
                           TrainingTypeRepository trainingTypeRepository,
                           UserRepository userRepository) {
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Training addTraining(@NotNull Trainer trainer, @NotNull Trainee trainee,
                                @NotNull String trainingName, @NotNull TrainingTypeEnum trainingTypeEnum,
                                @NotNull Date trainingDate, @NotNull Integer trainingDuration, @NotNull User admin) throws NotActiveException, AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(), admin.getPassword());
        if (!trainer.getUser().getActive() || !trainee.getUser().getActive()) {
            throw new NotActiveException("Trainer/Trainee is not active");
        }

        TrainingType trainingType = trainingTypeRepository.selectByType(trainingTypeEnum);
        if (trainer.getSpecialization().getTrainingTypeName() != trainingType.getTrainingTypeName()) {
            throw new IllegalArgumentException("Trainer has not that specialization");
        }

        Training training = new Training(trainer, trainee, trainingName, trainingType, trainingDate, trainingDuration);

        trainer.getTrainings().add(training);
        trainer.getTrainees().add(trainee);

        trainee.getTrainings().add(training);
        trainee.getTrainers().add(trainer);

        trainingRepository.save(training);
        trainerRepository.save(trainer);
        traineeRepository.save(trainee);
        return training;
    }
}
