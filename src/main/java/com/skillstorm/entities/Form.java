package com.skillstorm.entities;

import com.skillstorm.constants.EventType;
import com.skillstorm.constants.Status;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("form")
public class Form {

    @PrimaryKey
    private UUID id;

    private String username;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    private String email;

    private LocalTime time;

    private LocalDate date;

    private boolean urgent;

    private String location;

    private String description;

    private BigDecimal cost;

    //@Column("grade_format")
    //private GradeFormat gradeFormat;

    @Column("event_type")
    private EventType eventType;

    private String justification;

    private String attachment;

    @Column("supervisor_preapproval")
    private String supervisorAttachment;

    @Column("department_head_preapproval")
    private String departmentHeadAttachment;

    private Status status;

    @Column("excess_funds_approved")
    private boolean excessFundsApproved;

    private BigDecimal reimbursement;
}
