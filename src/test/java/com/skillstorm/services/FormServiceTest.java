package com.skillstorm.services;

import com.skillstorm.constants.EventType;
import com.skillstorm.constants.GradeFormat;
import com.skillstorm.constants.Status;
import com.skillstorm.dtos.FormDto;
import com.skillstorm.entities.Form;
import com.skillstorm.repositories.FormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class FormServiceTest {

    @InjectMocks private static FormServiceImpl formService;
    @Mock private static FormRepository formRepository;
    @Mock private static S3ServiceImpl s3Service;
    @Mock private static RabbitTemplate rabbitTemplate;

    private static Form returnedForm;
    private static FormDto newFormDto;

    @BeforeEach
    void setup() {
        formService = new FormServiceImpl(formRepository, s3Service, rabbitTemplate);
        System.out.println("\n\nSetup called");
        setupRequestForms();
    }

    private void setupRequestForms() {
        System.out.println("\n\nsetupRequestForms called");
        newFormDto = new FormDto();
        newFormDto.setUsername("testUser");
        newFormDto.setFirstName("TestFirstname");
        newFormDto.setLastName("TestLastname");
        newFormDto.setEmail("testUser@email.com");
        newFormDto.setTime("16:00");
        newFormDto.setLocation("Test Location");
        newFormDto.setDescription("Test Description");
        newFormDto.setCost(BigDecimal.valueOf(100));
        newFormDto.setEventType(EventType.UNIVERSITY_COURSE);
        newFormDto.setGradeFormat(GradeFormat.SCORE);
        newFormDto.setJustification("Test Justification");

        System.out.println("newFormDto instantiated");
        System.out.println("newFormDto: " + newFormDto.toString());

        returnedForm = new Form();
        returnedForm.setId(UUID.fromString("702772d8-f69f-45ca-870a-5d168bc27169"));
        returnedForm.setUsername("testUser");
        returnedForm.setFirstName("TestFirstname");
        returnedForm.setLastName("TestLastname");
        returnedForm.setEmail("testUser@email.com");
        returnedForm.setTime(LocalTime.parse("16:00"));
        returnedForm.setLocation("Test Location");
        returnedForm.setDescription("Test Description");
        returnedForm.setCost(BigDecimal.valueOf(100));
        returnedForm.setEventType(EventType.UNIVERSITY_COURSE);
        returnedForm.setGradeFormat(GradeFormat.SCORE);
        returnedForm.setPassingGrade("70");
        returnedForm.setJustification("Test Justification");
        returnedForm.setStatus(Status.CREATED);
        returnedForm.setReimbursement(BigDecimal.valueOf(80.00).setScale(2));

        System.out.println("Setup Finished");
        System.out.println("newFormDto: " + newFormDto.toString());
        System.out.println("returnedForm: " + returnedForm.toString());
    }

    // Create new Form success:
    //@Test
    void createFormTestSuccess() {

        System.out.println("\n\nTest method called");
        System.out.println("newFormDto: " + newFormDto.toString());

        when(formRepository.save(newFormDto.mapToEntity())).thenReturn(Mono.just(returnedForm));

        // Call method to test:
        Mono<FormDto> resultMono = formService.createForm(newFormDto);

        // Verify result:
        resultMono.doOnNext(result -> {
            assertEquals(UUID.fromString("702772d8-f69f-45ca-870a-5d168bc27169"), result.getId(), "ID should be 702772d8-f69f-45ca-870a-5d168bc27169");
            assertEquals("testUser", result.getUsername(), "Username should be testUser");
            assertEquals("TestFirstname", result.getFirstName(), "First name should be TestFirstname");
            assertEquals("TestLastname", result.getLastName(), "Last name should be TestLastname");
            assertEquals("testUser@email.com", result.getEmail(), "Email should be testUser@email.com");
            assertEquals("16:00", result.getTime(), "Time should be 16:00");
            assertEquals("Test Location", result.getLocation(), "Location should be Test Location");
            assertEquals(BigDecimal.valueOf(100).setScale(2), result.getCost(), "Cost should be 100.00");
            assertEquals(EventType.UNIVERSITY_COURSE, result.getEventType(), "Event type should be UNIVERSITY_COURSE");
            assertEquals(GradeFormat.SCORE, result.getGradeFormat(), "Grade Format should be SCORE");
            assertEquals("70", result.getPassingGrade(), "Passing Grade should be 70");
            assertEquals("Test Justification", result.getJustification(), "Justification should be Test Justification");
            assertEquals(Status.CREATED, result.getStatus(), "Status should be CREATED");
            assertEquals(BigDecimal.valueOf(80).setScale(2), result.getReimbursement(), "Reimbursement should be 80.00");
        });
    }
}
