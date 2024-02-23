package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.io.NotActiveException;
import java.nio.file.AccessDeniedException;
import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingType trainingType;
    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTraining() throws AccessDeniedException, AuthenticationException, NotActiveException {
        User trainerUser = new User("trainer", "trainer", true, RoleEnum.TRAINER);
        User traineeUser = new User("trainee", "trainee", true, RoleEnum.TRAINEE);
        User adminUser = new User("admin", "admin", true, RoleEnum.ADMIN);

        TrainingType trainingType = new TrainingType(TrainingTypeEnum.AGILITY);
        when(trainingTypeRepository.selectByType(TrainingTypeEnum.AGILITY)).thenReturn(trainingType);


        Trainer trainer = new Trainer(trainingType, trainerUser);
        Trainee trainee = new Trainee(null, null, traineeUser);
        Training training = trainingService.addTraining(trainer, trainee, "test", TrainingTypeEnum.AGILITY, Date.valueOf("2024-08-05"), 10, adminUser);

        assertNotNull(training);
        when(trainingRepository.save(training)).thenReturn(training);

        Training expected = trainingRepository.save(training);

        assertNotNull(expected);
    }
}