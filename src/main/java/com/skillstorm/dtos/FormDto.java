package com.skillstorm.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillstorm.constants.EventType;
import com.skillstorm.constants.Status;
import com.skillstorm.entities.Form;
import com.skillstorm.entities.GradeFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class FormDto {

    private UUID id;

    @NotNull(message = "{username.must}")
    @Size(min = 3, max = 25, message = "{username.size}")
    private String username;

    @NotNull(message = "{firstname.must}")
    @Size(min = 2, max = 50, message = "{firstname.size}")
    private String firstName;

    @NotNull(message = "{lastname.must}")
    @Size(min = 2, max = 50, message = "{lastname.size}")
    private String lastName;

    @NotNull(message = "{email.must")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{email.invalid}")
    private String email;

    //TODO: decide on a time format
    @NotNull(message = "{event.time.must}")
    private String time;

    //TODO: decide on a date format
    @NotNull(message = "{event.date.must}")
    private String date;

    private boolean urgent;

    @NotNull(message = "{event.location.must}")
    @NotEmpty(message = "{event.location.must}")
    private String location;

    @NotNull(message = "{event.description.must}")
    @Size(min = 5, max = 100, message = "{event.description.must}")
    private String description;

    @Min(value = 0, message = "{event.cost.must}")
    private BigDecimal cost;

    // TODO: Table entity or enum?
    //@NotNull(message = "{grade.format.must}")
    //private GradeFormat gradeFormat;

    @NotNull(message = "{event.type.must")
    private EventType eventType;

    @NotNull(message = "{event.justification.must}")
    @Size(min = 5, max = 100, message = "{event.justification.must}")
    private String justification;

    @Pattern(regexp = "^\\S+\\.(pdf|png|jpe?g|txt|doc)$", message = "{attachment.invalid}")
    private String attachment;

    @Pattern(regexp = "^\\S+\\.msg$", message = "{approval.attachment.invalid}")
    private String supervisorAttachment;

    @Pattern(regexp = "^\\S+\\.msg$", message = "{approval.attachment.invalid}")
    private String departmentHeadAttachment;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Status status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean excessFundsApproved;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal reimbursement;

    public FormDto() {
        this.id = UUID.randomUUID();
        this.status = Status.CREATED;
    }

    public FormDto(Form form) {
        super();
        this.id = form.getId();
        this.username = form.getUsername();
        this.firstName = form.getFirstName();
        this.lastName = form.getLastName();
        this.email = form.getEmail();
        this.time = form.getTime().toString();
        this.date = form.getDate().toString();
        this.urgent = form.isUrgent();
        this.location = form.getLocation();
        this.description = form.getDescription();
        this.cost = form.getCost();
        //this.gradeFormat = form.getGradeFormat();
        this.eventType = form.getEventType();
        this.justification = form.getJustification();
        this.attachment = form.getAttachment();
        this.supervisorAttachment = form.getSupervisorAttachment();
        this.departmentHeadAttachment = form.getDepartmentHeadAttachment();
        this.status = form.getStatus();
        this.excessFundsApproved = form.isExcessFundsApproved();
        this.reimbursement = form.getReimbursement();
    }

    @JsonIgnore
    public Form mapToEntity() {
        Form form = new Form();
        form.setId(id);
        form.setUsername(username);
        form.setFirstName(firstName);
        form.setLastName(lastName);
        form.setEmail(email);
        form.setTime(LocalTime.parse(time));
        form.setDate(LocalDate.parse(date));
        form.setUrgent(urgent);
        form.setLocation(location);
        form.setDescription(description);
        form.setCost(cost);
        //form.setGradeFormat(gradeFormat);
        form.setEventType(eventType);
        form.setJustification(justification);
        form.setAttachment(attachment);
        form.setSupervisorAttachment(supervisorAttachment);
        form.setDepartmentHeadAttachment(departmentHeadAttachment);
        form.setStatus(status);
        form.setExcessFundsApproved(excessFundsApproved);
        form.setReimbursement(reimbursement);

        return form;
    }
}
