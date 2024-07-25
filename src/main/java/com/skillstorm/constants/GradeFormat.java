package com.skillstorm.constants;

import lombok.Getter;

@Getter
public enum GradeFormat {
    SCORE("70"), PRESENTATION("presentation"), PASS_FAIL("pass"), OTHER("presentation");

    private final String defaultScore;

    GradeFormat(String defaultScore) {
        this.defaultScore = defaultScore;
    }
}
