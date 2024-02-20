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

import java.util.Date;
import java.util.List;

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
    @Transactional
    public Trainer createProfile(@NotNull String firstName,@NotNull String lastName,
                              @NotNull Boolean isActive,@NotNull TrainingTypeEnum trainingTypeEnum){
        User trainerUser = new User(firstName,lastName,isActive);
        if (!userRepository.usernameExists(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false))){
            trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), false));
        }else {
            trainerUser.setUsername(generateUsername(trainerUser.getFirstName(), trainerUser.getLastName(), true));
        }
        Trainer trainer = new Trainer(trainingTypeRepository.selectByType(trainingTypeEnum),trainerUser);
        trainerRepository.save(trainer);
        return trainer;
    }

    public Trainer selectProfile(@NotNull String newUsername,@NotNull User currentUser) throws AuthenticationException {
        userRepository.authenticate(currentUser.getUsername(),currentUser.getPassword());
        return trainerRepository.selectByUsername(newUsername);
    }
    public void changePassword(@NotNull String newPassword,@NotNull Trainer currentTrainer) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(),currentTrainer.getUser().getPassword());
        userRepository.changePassword(newPassword,currentTrainer.getUser().getUserId());
        currentTrainer.getUser().setPassword(newPassword);
    }
    public void updateTrainer(@NotNull TrainingType newSpecialization,@NotNull Trainer currentTrainer) throws AuthenticationException {
        userRepository.authenticate(currentTrainer.getUser().getUsername(), currentTrainer.getUser().getPassword());
        trainerRepository.updateTrainer(newSpecialization);
        currentTrainer.setSpecialization(newSpecialization);
    }
    public void activateDeactivate(@NotNull Trainer currentTrainer) throws AuthenticationException {
        User currentUser = currentTrainer.getUser();
        userRepository.authenticate(currentUser.getUsername(), currentUser.getPassword());
        currentUser.setActive(!currentUser.getActive());
        userRepository.activateDeactivate(currentUser.getActive(), currentUser.getUserId());
    }
    @Transactional
    public List<Training> getTrainingList(@NotNull Trainer currentTrainee, Date fromDate, Date toDate,
                                          String traineeName, TrainingTypeEnum trainingTypeEnum) throws AuthenticationException {
        userRepository.authenticate(currentTrainee.getUser().getUsername(), currentTrainee.getUser().getPassword());
        return trainerRepository.getTrainingList(currentTrainee.getUser().getUsername(),
                fromDate,toDate,traineeName,trainingTypeEnum);
    }


}
