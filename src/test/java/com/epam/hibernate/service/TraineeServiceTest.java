package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TraineeRepository;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.TrainingTypeRepository;
import com.epam.hibernate.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @InjectMocks
    private TraineeService traineeService;
    @Test
    void createProfile() {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);
        assertNotNull(trainee);

        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        assertEquals(trainee, traineeRepository.save(trainee));
        assertEquals(trainee.getUser().getUsername(), "test.test");
    }
    @Test
    void createProfileWithSameUsername() {
        Trainee trainee1 = traineeService.createProfile("test", "test", true, null,null);

        when(userRepository.usernameExists("test.test")).thenReturn(true);

        Trainee trainee2 = traineeService.createProfile("test", "test", true, null,null);

        assertNotEquals(trainee1.getUser().getUsername(), trainee2.getUser().getUsername());
    }
    @Test
    void selectCurrentTraineeProfile() throws AuthenticationException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        when(traineeRepository.selectByUsername(trainee.getUser().getUsername())).thenReturn(trainee);

        Trainee result = traineeService.selectCurrentTraineeProfile(trainee);

        assertNotNull(result);
        assertEquals(trainee, result);
    }
    @Test
    void selectProfile() throws AuthenticationException, AccessDeniedException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        User admin = new User("admin", "admin", true, RoleEnum.ADMIN);
        when(traineeRepository.selectByUsername(trainee.getUser().getUsername())).thenReturn(trainee);

        Trainee result = traineeService.selectProfile(trainee.getUser().getUsername(), admin);

        assertNotNull(result);
        assertEquals(trainee, result);
    }
    @Test
    void changePassword() throws AuthenticationException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        traineeService.changePassword("password", trainee);

        assertEquals("password", trainee.getUser().getPassword());
    }

    @Test
    void updateTrainee() throws AuthenticationException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        traineeService.updateTrainee(null, "Tbilisi", trainee);

        assertEquals("Tbilisi", trainee.getAddress());
    }

    @Test
    void activateDeactivate() throws AuthenticationException, AccessDeniedException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        User admin = new User("admin", "admin", true, RoleEnum.ADMIN);

        traineeService.activateDeactivate(trainee,admin);

        assertEquals(false, trainee.getUser().getActive());
    }

    @Test
    void deleteTrainee() throws AuthenticationException, AccessDeniedException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);
        User admin = new User("admin", "admin", true, RoleEnum.ADMIN);

        traineeService.deleteTrainee(trainee.getUser().getUsername(), admin);

        assertNull(traineeService.selectProfile(trainee.getUser().getUsername(),admin));
    }

    @Test
    void getTrainingList() throws AuthenticationException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);
        Training training1 = new Training();
        Training training2 = new Training();

        trainee.addTraining(training1);
        trainee.addTraining(training2);

        when(traineeRepository.getTrainingList(trainee.getUser().getUsername(), null, null, null, null))
                .thenReturn(List.of(training1, training2));

        List<Training> trainings = traineeService.getTrainingList(trainee, null, null, null, null);
        assertEquals(trainee.getTrainings().size(), trainings.size());
    }

    @Test
    void notAssignedTrainersList() throws AuthenticationException {
        Trainee trainee = traineeService.createProfile("test", "test", true, null,null);

        Trainer trainer1 = new Trainer(new TrainingType(TrainingTypeEnum.AGILITY),
                new User("trainer1","trainer1",true,RoleEnum.TRAINER));
        Trainer trainer2 = new Trainer(new TrainingType(TrainingTypeEnum.AGILITY),
                new User("trainer2","trainer2",true,RoleEnum.TRAINER));

        trainerRepository.save(trainer1);
        trainerRepository.save(trainer2);

        System.out.println(trainer1);


        when(trainerRepository.getAllTrainers()).thenReturn(List.of(trainer1,trainer2));

        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer1);
        trainers.add(trainer2);

        List<Trainer> expected = traineeService.notAssignedTrainersList(trainee.getUser(),trainee.getUser().getUsername());

        assertEquals(trainers.size(),expected.size());
    }
}