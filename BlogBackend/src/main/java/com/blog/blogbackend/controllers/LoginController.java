package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.DTOs.AuthResponseDTO;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;
import com.blog.blogbackend.services.AuthService;
import com.blog.blogbackend.services.UserService;
import com.blog.blogbackend.utils.DTOValidationResultHandler;
import com.blog.blogbackend.utils.FieldErrorsExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user/login")
public class LoginController {

    private final AuthService authService;
    private final String defaultErrorMessage = "Username and password are required.";

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> requestBodyNotValid(MethodArgumentNotValidException e) {

        DTOValidationResultHandler resultHandler = new DTOValidationResultHandler(defaultErrorMessage);
        Map<String, String> result = resultHandler.getResultsForInvalidFields(e);

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map> noRequestBody() {

        Map<String, String> result = new HashMap<>();
        result.put("error", defaultErrorMessage);

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map> badCredentialsError(Exception e) {

        Map<String, String> result = new HashMap<>();

        result.put("error", "Username or password is incorrect.");

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> otherErrors(Exception e) {

        Map<String, String> result = new HashMap<>();
        result.put("error", e.getMessage());

        return ResponseEntity.status(401).body(result);
    }

    @PostMapping
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginUserDTO loginData) {

        AuthResponseDTO response = authService.loginUser(loginData);


        return ResponseEntity.ok(response);
    }
}
