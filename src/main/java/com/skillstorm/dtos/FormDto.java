package com.skillstorm.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skillstorm.entities.Form;
import lombok.Data;

import java.util.UUID;

@Data
public class FormDto {

    private UUID id;

    public FormDto() {
        this.id = UUID.randomUUID();
    }

    public FormDto(Form form) {
        super();
        this.id = form.getId();
    }

    @JsonIgnore
    public Form mapToEntity() {
        Form form = new Form();
        form.setId(id);

        return form;
    }
}
