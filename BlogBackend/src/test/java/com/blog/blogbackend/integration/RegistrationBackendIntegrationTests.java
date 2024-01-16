package com.blog.blogbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.blogbackend.models.DTOs.NewUserDTO;
import com.blog.blogbackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class RegistrationBackendIntegrationTests {

    private ObjectMapper om;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        om = new ObjectMapper();
    }

    @AfterAll
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void POSTregisterWithValidUserData() throws Exception {
        NewUserDTO newUserData = new NewUserDTO(
                "johnunique", "password123");

        mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newUserData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User johnunique successfully created.")));
    }

    @Test
    public void POSTregisterWithMissingUsername() throws Exception {
        NewUserDTO newUserData = new NewUserDTO();
        newUserData.setPassword("password123");

        mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newUserData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username is required.")));
    }

    @Test
    public void POSTregisterWithNoData() throws Exception {

        mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username and password are required.")));
    }

    @Test
    public void POSTregisterWithNoRequestBody() throws Exception {

        mockMvc.perform(
                        post("/user/register"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username and password are required.")));
    }

    @Test
    public void POSTregisterWithPasswordUnder6Characters() throws Exception {
        NewUserDTO newUserData = new NewUserDTO(
                "johndoe", "pass");


        mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newUserData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Password must contain at least 6 characters.")));
    }

    @Test
    public void POSTregisterWithUsernameAlreadyTaken() throws Exception {
        NewUserDTO newUserData = new NewUserDTO(
                "johndoe", "password123");

        NewUserDTO anotherNewUserData = new NewUserDTO(
                "johndoe", "password456");


        mockMvc.perform(
                post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newUserData)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/user/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newUserData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error", is("Username is already taken.")));
    }

}
