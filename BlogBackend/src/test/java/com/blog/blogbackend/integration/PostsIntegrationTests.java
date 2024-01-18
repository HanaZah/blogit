package com.blog.blogbackend.integration;

import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.Role;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.PostRepository;
import com.blog.blogbackend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostsIntegrationTests {
    private ObjectMapper om;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        om = new ObjectMapper();
    }

    @AfterAll
    public void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User prepareUser() {
        if(userRepository.findUserByUsernameAndDeleted("postsIntTest", false).isEmpty()) {
            User newUser = new User("postsIntTest", passwordEncoder.encode("password"));
            newUser.setRole(Role.ROLE_USER);

            return userRepository.save(newUser);
        }

        return userRepository.findUserByUsernameAndDeleted("postsIntTest", false).get();
    }

    private void mockAuthenticationContext(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private void preparePosts() {

        User user = prepareUser();

        if(postRepository.findByTitleAndDeleted("postsIntTest1", false).isEmpty()) {
            Post newPost1 = new Post("postsIntTest1", "test content", user);
            postRepository.save(newPost1);
        }

        if(postRepository.findByTitleAndDeleted("postsIntTest2", false).isEmpty()) {
            Post newPost2 = new Post("postsIntTest2", "more test content", user);
            postRepository.save(newPost2);
        }
    }

    @Test
    public void GETpostsWithAuthenticatedRequest() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());

        mockMvc.perform(
                get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").exists())
                .andExpect(jsonPath("$.posts[0].title").exists())
                .andExpect(jsonPath("$.posts[0].content").exists())
                .andExpect(jsonPath("$.posts[0].author").exists());

    }

    @Test
    public void GETpostsWithUnauthenticatedRequest() throws Exception {
        mockMvc.perform(
                        get("/posts"))
                .andExpect(status().is(403));
    }

    @Test
    public void GETpostsWithExistingId() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        get("/posts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").value(post.getContent()));

    }

    @Test
    public void GETpostsWithNonexistentId() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());

        mockMvc.perform(
                        get("/posts/12"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Post ID does not match."));
    }

    @Test
    public void POSTpostsWithValidData() throws Exception {
        User user = prepareUser();
        mockAuthenticationContext(user);
        NewPostDTO newPostData = new NewPostDTO("Whole new", "content for you");

        mockMvc.perform(
                post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newPostData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value(newPostData.getTitle()))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").value(newPostData.getContent()))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.username").value(user.getUsername()));
    }

    @Test
    public void POSTpostsWithMissingTitle() throws Exception {
        mockAuthenticationContext(prepareUser());
        NewPostDTO newPostData = new NewPostDTO();
        newPostData.setContent("content for you");

        mockMvc.perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newPostData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Title is required."));
    }

    @Test
    public void POSTpostsWithMissingContent() throws Exception {
        mockAuthenticationContext(prepareUser());
        NewPostDTO newPostData = new NewPostDTO();
        newPostData.setTitle("Whole new");

        mockMvc.perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(newPostData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Content is required."));
    }

    @Test
    public void POSTpostsWithEmptyRequestBody() throws Exception {
        mockAuthenticationContext(prepareUser());

        mockMvc.perform(
                        post("/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Title and content are required."));
    }

    @Test
    public void POSTpostsWithNoRequestBody() throws Exception {
        mockAuthenticationContext(prepareUser());

        mockMvc.perform(
                        post("/posts"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Title and content are required."));
    }

    @Test
    public void PATCHpostsWithExistingIdAndValidData() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        NewPostDTO updateData = new NewPostDTO("Update title", "update content");
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                patch("/posts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Update title"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").value("update content"));
    }

    @Test
    public void PATCHpostsWithExistingIdAndMissingTitle() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        NewPostDTO updateData = new NewPostDTO();
        updateData.setContent("update content");
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        patch("/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(updateData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Title is required."));
    }

    @Test
    public void PATCHpostsWithExistingIdAndMissingContent() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        NewPostDTO updateData = new NewPostDTO();
        updateData.setTitle("Update title");
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        patch("/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(updateData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Content is required."));
    }

    @Test
    public void PATCHpostsWithExistingIdAndNotByAuthor() throws Exception {
        preparePosts();
        User newUser = new User("notauthor", "password");
        newUser.setRole(Role.ROLE_USER);
        mockAuthenticationContext(newUser);
        NewPostDTO updateData = new NewPostDTO("Update title", "update content");
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        patch("/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(updateData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Only the author can edit this post."));
    }

    @Test
    public void PATCHpostsWithNonexistentId() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        NewPostDTO updateData = new NewPostDTO("Update title", "update content");
        Long id = 12345L;

        mockMvc.perform(
                        patch("/posts/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(updateData)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Post ID does not match."));
    }

    @Test
    public void DELETEpostsWithExistingIdAndByAuthor() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        delete("/posts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Post has been successfully deleted."));
    }

    @Test
    public void DELETEpostsWithExistingIdAndNotByAuthor() throws Exception {
        preparePosts();
        User newUser = new User("notauthor", "password");
        newUser.setRole(Role.ROLE_USER);
        mockAuthenticationContext(newUser);
        Post post = postRepository.findAllByDeletedOrderByRatingDesc(false).get(0);
        Long id = post.getId();

        mockMvc.perform(
                        delete("/posts/" + id))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Only the author can edit this post."));
    }

    @Test
    public void DELETEpostsWithNonexistentId() throws Exception {
        preparePosts();
        mockAuthenticationContext(prepareUser());
        Long id = 12345L;

        mockMvc.perform(
                        delete("/posts/" + id))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value("Post ID does not match."));
    }
}
