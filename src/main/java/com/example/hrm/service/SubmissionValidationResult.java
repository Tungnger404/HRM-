package com.example.hrm.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubmissionValidationResult {

    private final List<String> errors = new ArrayList<>();

    public static SubmissionValidationResult valid() {
        return new SubmissionValidationResult();
    }

    public void addError(String error) {
        if (error != null && !error.isBlank()) {
            errors.add(error);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
