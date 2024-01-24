package com.blog.blogbackend.utils;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

public class DTOValidationResultHandler {

    private FieldErrorsExtractor errorsExtractor;
    private String defaultMessage;

    public DTOValidationResultHandler(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public void setExtractor(MethodArgumentNotValidException exception) {
        errorsExtractor = new FieldErrorsExtractor(exception);
    }

    public FieldErrorsExtractor getErrorsExtractor() {
        return errorsExtractor;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public Map<String, String> getResultsForInvalidFields(MethodArgumentNotValidException exception) {
        Map<String, String> result = new HashMap<>();
        setExtractor(exception);
        String message = (errorsExtractor.getFailedFields().size() == 1)?
                errorsExtractor.getFirstError().getDefaultMessage() : defaultMessage;

        result.put("error", message);

        return result;
    }
}
