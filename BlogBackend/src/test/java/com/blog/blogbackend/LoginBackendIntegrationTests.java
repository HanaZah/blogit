package com.blog.blogbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.blogbackend.models.DTOs.LoginUserDTO;
import com.blog.blogbackend.models.Role;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties")
public class LoginBackendIntegrationTests {

    private User user = new User();
    private ObjectMapper om;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        om = new ObjectMapper();
        setUpUser();
    }

    @AfterAll
    public void tearDown() {
        userRepository.deleteAll();
    }

    private void setUpUser() {
        if(userRepository.findUserByUsernameAndDeleted("johndoe", false).isEmpty()) {
            user.setUsername("johndoe");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
        }
    }

    @Test
    void contextLoads() {
    }

    @Test
    void POSTloginWithValidCredentials() throws Exception {
        LoginUserDTO request = new LoginUserDTO();
        request.setUsername("johndoe");
        request.setPassword("password");

        mockMvc.perform(
                post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("johndoe")))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void POSTloginWithWrongCredentials() throws Exception {
        LoginUserDTO request = new LoginUserDTO();
        request.setUsername("johndoe");
        request.setPassword("passwordNot");

        mockMvc.perform(
                        post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(request)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username or password is incorrect.")));
    }

    @Test
    void POSTloginWithMissingPassword() throws Exception {
        LoginUserDTO request = new LoginUserDTO();
        request.setUsername("johndoe");

        mockMvc.perform(
                        post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(request)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Password is required.")));
    }

    @Test
    void POSTloginWithMissingUsername() throws Exception {
        LoginUserDTO request = new LoginUserDTO();
        request.setPassword("password");

        mockMvc.perform(
                        post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(request)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username is required.")));
    }

    @Test
    void POSTloginWithEmptyRequestBody() throws Exception {

        mockMvc.perform(
                        post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username and password are required.")));
    }

    @Test
    void POSTloginWithNoRequestBody() throws Exception {

        mockMvc.perform(
                        post("/user/login"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username and password are required.")));
    }
}
