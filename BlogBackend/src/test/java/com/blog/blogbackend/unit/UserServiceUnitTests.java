package com.blog.blogbackend.unit;

import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
import com.blog.blogbackend.services.UserService;
import com.blog.blogbackend.services.UserServiceImpl;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserServiceUnitTests {
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, passwordEncoder);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
    }

    @Test
    public void createReturnsNewUserForPasswordAndUniqueUsername() throws Exception {
        NewUserDTO userData = new NewUserDTO("johnUnique", "password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.create(userData);

        assertEquals(userData.getUsername(), user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
    }

    @Test
    public void createThrowsEntityExistsExceptionForNonUniqueUsername() {
        NewUserDTO userData = new NewUserDTO("johntest", "password");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(EntityExistsException.class, () -> userService.create(userData));
    }

    @Test
    public void loadUserByUsernameReturnsUserDetailsForExistingUsername() {
        User user = new User("johntest", "password");
        user.setId(1L);
        when(userRepository.findUserByUsernameAndDeleted("johntest", false))
                .thenReturn(Optional.of(user));

        UserDetails foundUser = userService.loadUserByUsername("johntest");

        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getPassword(), foundUser.getPassword());
    }

    @Test
    public void loadUserByUsernameThrowsUsernameNotFoundExceptionForNonexistentUsername() {
        when(userRepository.findUserByUsernameAndDeleted("johnNotReal", false))
                .thenThrow(new UsernameNotFoundException("Username is incorrect."));

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("johnNotReal"));
    }

    @Test
    public void softDeleteMarksUserAsDeleted() {
        User user = new User("johntest", "password");
        List<User> fakeRepository = new ArrayList<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            fakeRepository.add(savedUser);
            return savedUser;
        });

        userService.softDelete(user);

        assertEquals(user.getUsername(), fakeRepository.get(0).getUsername());
        assertTrue(fakeRepository.get(0).isDeleted());
    }
}
