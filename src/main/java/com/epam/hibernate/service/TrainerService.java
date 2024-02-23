package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.TrainingTypeRepository;
import com.epam.hibernate.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.List;

import static com.epam.hibernate.Utils.checkAdmin;
import static com.epam.hibernate.Utils.generateUsername;

@Service
public class TrainerService {
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    @Autowired
    public TrainerService(TrainingTypeRepository trainingTypeRepository,
                          TrainerRepository trainerRepository,
                          UserRepository userRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
    }

    public Trainer createProfile(@NotNull String firstName, @NotNull String lastName,
                                 @NotNull Boolean isActive, @NotNull TrainingTypeEnum trainingTypeEnum) {
        User trainerUser = new User(firstName, lastName, isActive, RoleEnum.TRAINER);
        if (!userRepository.usernameExists(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false))) {
            trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false));
        } else {
            trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), true));
        }
        Trainer trainer = new Trainer(trainingTypeRepository.selectByType(trainingTypeEnum), trainerUser);
        trainerRepository.save(trainer);
        return trainer;
    }

    public Trainer selectCurrentTrainerProfile(@NotNull Trainer currentTrainer) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(), currentTrainer.getUser().getPassword());
        return trainerRepository.selectByUsername(currentTrainer.getUser().getUsername());
    }

    public Trainer selectProfile(@NotNull String newUsername, @NotNull User admin) throws AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(), admin.getPassword());
        return trainerRepository.selectByUsername(newUsername);
    }

    public void changePassword(@NotNull String newPassword, @NotNull Trainer currentTrainer) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(), currentTrainer.getUser().getPassword());
        userRepository.changePassword(newPassword, currentTrainer.getUser().getUserId());
        currentTrainer.getUser().setPassword(newPassword);
    }

    public void updateTrainer(@NotNull TrainingType newSpecialization, @NotNull Trainer currentTrainer) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(), currentTrainer.getUser().getPassword());
        trainerRepository.updateTrainer(newSpecialization, currentTrainer.getTrainerId());
        currentTrainer.setSpecialization(newSpecialization);
    }

    public void activateDeactivate(@NotNull Trainer currentTrainer, @NotNull User admin) throws AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(), admin.getPassword());
        currentTrainer.getUser().setActive(!currentTrainer.getUser().getActive());
        userRepository.activateDeactivate(currentTrainer.getUser().getActive(), currentTrainer.getUser().getUserId());
    }

    @Transactional
    public List<Training> getTrainingList(@NotNull Trainer currentTrainer, Date fromDate, Date toDate,
                                          String traineeName, TrainingTypeEnum trainingTypeEnum) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(), currentTrainer.getUser().getPassword());
        return trainerRepository.getTrainingList(currentTrainer.getUser().getUsername(),
                fromDate, toDate, traineeName, trainingTypeEnum);
    }


}
