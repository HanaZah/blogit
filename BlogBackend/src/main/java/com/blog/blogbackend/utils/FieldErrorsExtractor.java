package com.blog.blogbackend.utils;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

public class FieldErrorsExtractor {
    List<FieldError> fieldErrors;

    List<String> failedFields;

    public FieldErrorsExtractor(MethodArgumentNotValidException exception) {

        fieldErrors = exception
                .getBindingResult()
                .getFieldErrors();

        failedFields = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .distinct()
                .toList();
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public List<String> getFailedFields() {
        return failedFields;
    }

    public void setFailedFields(List<String> failedFields) {
        this.failedFields = failedFields;
    }

    public FieldError getFirstError() {
        return fieldErrors.get(0);
    }
}
