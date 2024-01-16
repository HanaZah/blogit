package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.DTOs.AuthResponseDTO;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;
import com.blog.blogbackend.services.AuthService;
import com.blog.blogbackend.services.UserService;
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

    private final UserService userService;
    private final AuthService authService;

    public LoginController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> requestBodyNotValid(MethodArgumentNotValidException e) {

        Map<String, String> response = new HashMap<>();
        FieldErrorsExtractor extractor = new FieldErrorsExtractor(e);
        String message = (extractor.getFailedFields().size() == 1)? extractor.getFirstError().getDefaultMessage()
                : "Username and password are required.";

        response.put("error", message);

        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map> noRequestBody() {

        Map<String, String> result = new HashMap<>();
        result.put("error", "Username and password are required.");

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
