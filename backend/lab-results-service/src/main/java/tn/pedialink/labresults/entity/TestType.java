package tn.pedialink.labresults.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TestType {
    BLOOD,
    URINE,
    BIOPSY,
    OTHER;

    @JsonCreator
    public static TestType fromValue(String value) {
        if (value == null) return OTHER;
        try {
            return TestType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
