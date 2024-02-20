package com.epam.hibernate;


import com.epam.hibernate.entity.*;
import com.epam.hibernate.repository.TrainerRepository;
import com.epam.hibernate.repository.TrainingTypeRepository;
import com.epam.hibernate.repository.UserRepository;
import com.epam.hibernate.service.TraineeService;
import com.epam.hibernate.service.TrainerService;
import com.epam.hibernate.service.TrainingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.naming.AuthenticationException;
import java.io.NotActiveException;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class HibernateApplication {

	public static void main(String[] args) throws InterruptedException, AuthenticationException, NotActiveException {
		ApplicationContext context = SpringApplication.run(HibernateApplication.class, args);

		TrainingTypeRepository trainingTypeRepository = context.getBean(TrainingTypeRepository.class);
		trainingTypeRepository.addTrainingTypes();

		TrainerService trainerService = context.getBean(TrainerService.class);
		TraineeService traineeService = context.getBean(TraineeService.class);
		TrainingService trainingService = context.getBean(TrainingService.class);

		Trainer trainer = trainerService.createProfile("blah", "bluh",
				true, TrainingTypeEnum.BALANCE);
		Trainer trainer2 = trainerService.createProfile("second", "trainer",
				true, TrainingTypeEnum.AGILITY);

		Trainee trainee = traineeService.createProfile("blah", "bluh",
				true, null, "Tbilisi, Georgia");

		System.out.println(traineeService.notAssignedTrainersList(trainee.getUser(),trainee.getUser().getUsername()));
		Thread.currentThread().join();
	}

}
