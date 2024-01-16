package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.services.UserService;
import com.blog.blogbackend.utils.DTOValidationResultHandler;
import com.blog.blogbackend.utils.FieldErrorsExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user/register")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> requestBodyNotValid(MethodArgumentNotValidException e) {

        DTOValidationResultHandler resultHandler = new DTOValidationResultHandler(
                "Username and password are required."
        );
        Map<String, String> result = resultHandler.getResultsForInvalidFields(e);

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map> noRequestBody() {

        Map<String, String> result = new HashMap<>();
        result.put("error", "Username and password are required.");

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> otherErrors(Exception e) {

        Map<String, String> result = new HashMap<>();
        result.put("error", e.getMessage());

        return ResponseEntity.status(401).body(result);
    }

    @PostMapping
    public ResponseEntity<Map> registerUser(@RequestBody @Valid NewUserDTO userData) throws Exception {

        Map<String, String> result = new HashMap<>();
        User createdUser = userService.createNewUser(userData);

        result.put("message", "User " + createdUser.getUsername() + " successfully created.");

        return ResponseEntity.ok(result);
    }
}
