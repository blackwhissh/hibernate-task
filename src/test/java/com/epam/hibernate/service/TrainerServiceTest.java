package com.epam.hibernate.service;

import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.TrainingTypeRepository;
import com.epam.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createProfile() {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);
        assertNotNull(trainer);

        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        assertEquals(trainer, trainerRepository.save(trainer));
        assertEquals(trainer.getUser().getUsername(), "test.test");
    }

    @Test
    void createProfileWithSameUsername() {
        Trainer trainer1 = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        when(userRepository.usernameExists("test.test")).thenReturn(true);

        Trainer trainer2 = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        assertNotEquals(trainer1.getUser().getUsername(), trainer2.getUser().getUsername());
    }

    @Test
    void selectProfile() throws AuthenticationException, AccessDeniedException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        User user = new User("admin", "admin", true, RoleEnum.ADMIN);
        when(trainerRepository.selectByUsername(trainer.getUser().getUsername())).thenReturn(trainer);

        Trainer result = trainerService.selectProfile(trainer.getUser().getUsername(), user);

        assertNotNull(result);
        assertEquals(trainer, result);
    }

    @Test
    void selectCurrentTrainerProfile() throws AuthenticationException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        when(trainerRepository.selectByUsername(trainer.getUser().getUsername())).thenReturn(trainer);

        Trainer result = trainerService.selectCurrentTrainerProfile(trainer);

        assertNotNull(result);
        assertEquals(trainer, result);

    }

    @Test
    void selectProfileAccessDenied() {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        User user = new User("admin", "admin", true, RoleEnum.TRAINER);

        assertThrows(AccessDeniedException.class, () -> trainerService.selectProfile(trainer.getUser().getUsername(), user));
    }

    @Test
    void changePassword() throws AuthenticationException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        trainerService.changePassword("password", trainer);

        assertEquals("password", trainer.getUser().getPassword());
    }

    @Test
    void updateTrainer() throws AuthenticationException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);

        when(trainingTypeRepository.selectByType(TrainingTypeEnum.AGILITY)).thenReturn(new TrainingType(TrainingTypeEnum.AGILITY));

        TrainingType trainingType = trainingTypeRepository.selectByType(TrainingTypeEnum.AGILITY);

        trainerService.updateTrainer(trainingType, trainer);

        assertEquals(TrainingTypeEnum.AGILITY, trainer.getSpecialization().getTrainingTypeName());
    }

    @Test
    void activateDeactivate() throws AuthenticationException, AccessDeniedException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);
        User admin = new User("admin", "admin", true, RoleEnum.ADMIN);

        trainerService.activateDeactivate(trainer, admin);

        assertEquals(false, trainer.getUser().getActive());
    }

    @Test
    void getTrainingList() throws AuthenticationException {
        Trainer trainer = trainerService.createProfile("test", "test", true, TrainingTypeEnum.BALANCE);
        Training training1 = new Training();
        Training training2 = new Training();

        trainer.addTraining(training1);
        trainer.addTraining(training2);

        when(trainerRepository.getTrainingList(trainer.getUser().getUsername(), null, null, null, null)).thenReturn(List.of(training1, training2));

        List<Training> trainings = trainerService.getTrainingList(trainer, null, null, null, null);
        assertEquals(trainer.getTrainings().size(), trainings.size());


    }
}