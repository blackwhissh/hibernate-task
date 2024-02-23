package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TraineeRepository;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.epam.hibernate.Utils.checkAdmin;
import static com.epam.hibernate.Utils.generateUsername;

@Service
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    @Autowired
    public TraineeService(TraineeRepository traineeRepository, UserRepository userRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
    }
    public Trainee createProfile(@NotNull String firstName,@NotNull String lastName,@NotNull Boolean isActive,
                              Date dob, String address){
        User traineeUser = new User(firstName,lastName,isActive,RoleEnum.TRAINEE);
        if (!userRepository.usernameExists(generateUsername(traineeUser.getFirstName(), traineeUser.getLastName(), false))){
            traineeUser.setUsername(generateUsername(traineeUser.getFirstName(), traineeUser.getLastName(), false));
        }else {
            traineeUser.setUsername(generateUsername(traineeUser.getFirstName(), traineeUser.getLastName(), true));
        }
        Trainee trainee = new Trainee(dob,address,traineeUser);
        traineeRepository.save(trainee);
        return trainee;
    }
    public Trainee selectCurrentTraineeProfile(@NotNull Trainee currentTrainee) throws AuthenticationException {
        userRepository.authenticate(currentTrainee.getUser().getUsername(),currentTrainee.getUser().getPassword());
        return traineeRepository.selectByUsername(currentTrainee.getUser().getUsername());
    }
    public Trainee selectProfile(String newUsername, @NotNull User admin) throws AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(),admin.getPassword());
        return traineeRepository.selectByUsername(newUsername);
    }
    public void changePassword(@NotNull String newPassword,@NotNull Trainee currentTrainee) throws AuthenticationException {
        userRepository.authenticate(currentTrainee.getUser().getUsername(),currentTrainee.getUser().getPassword());
        userRepository.changePassword(newPassword,currentTrainee.getUser().getUserId());
        currentTrainee.getUser().setPassword(newPassword);
    }
    public void updateTrainee(Date dob, String address, @NotNull Trainee currentTrainee) throws AuthenticationException {
        userRepository.authenticate(currentTrainee.getUser().getUsername(), currentTrainee.getUser().getPassword());
        traineeRepository.updateTrainee(dob, address);
        if(dob != null){
            currentTrainee.setDob(dob);
        }
        if(address != null){
            currentTrainee.setAddress(address);
        }
    }
    public void activateDeactivate(@NotNull Trainee currentTrainee, @NotNull User admin) throws AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(),admin.getPassword());
        currentTrainee.getUser().setActive(!currentTrainee.getUser().getActive());
        userRepository.activateDeactivate(currentTrainee.getUser().getActive(), currentTrainee.getUser().getUserId());
    }
    @Transactional
    public void deleteTrainee(@NotNull String username, @NotNull User admin) throws AuthenticationException, AccessDeniedException {
        checkAdmin(admin);
        userRepository.authenticate(admin.getUsername(), admin.getPassword());
        traineeRepository.deleteTrainee(username);
    }
    @Transactional
    public List<Training> getTrainingList(@NotNull Trainee currentTrainee, Date fromDate, Date toDate,
                                          String trainerName, TrainingTypeEnum trainingTypeEnum) throws AuthenticationException {
        userRepository.authenticate(currentTrainee.getUser().getUsername(), currentTrainee.getUser().getPassword());
        return traineeRepository.getTrainingList(currentTrainee.getUser().getUsername(),
                fromDate,toDate,trainerName,trainingTypeEnum);
    }
    @Transactional
    public List<Trainer> notAssignedTrainersList(@NotNull User currentUser, @NotNull String traineeUsername) throws AuthenticationException {
        userRepository.authenticate(currentUser.getUsername(),currentUser.getPassword());
        Trainee trainee = traineeRepository.selectByUsername(traineeUsername);
        List<Trainer> allTrainers = trainerRepository.getAllTrainers();
        List<Trainer> notAssignedTrainers = new ArrayList<>();
        for(Trainer trainer : allTrainers){
            if(!trainer.getTrainees().contains(trainee)){
                notAssignedTrainers.add(trainer);
            }
        }
        return notAssignedTrainers;
    }

}
