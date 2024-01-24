package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.services.PostService;
import com.blog.blogbackend.utils.DTOValidationResultHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final String defaultErrorMessage = "Title and content are required.";

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map> requestBodyNotValid(MethodArgumentNotValidException e) {
        DTOValidationResultHandler resultHandler = new DTOValidationResultHandler(defaultErrorMessage);
        Map<String, String> result = resultHandler.getResultsForInvalidFields(e);

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map> missingRequestBodyParts() {
        Map<String, String> result = new HashMap<>();
        result.put("error", defaultErrorMessage);

        return ResponseEntity.status(401).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> otherErrors(Exception e) {
        Map<String, String> result = new HashMap<>();
        result.put("error", e.getMessage());

        return ResponseEntity.status(401).body(result);
    }

    @GetMapping
    public ResponseEntity<Map> getAllPosts() {
        Map<String, List<PostOverviewDTO>> result = new HashMap<>();
        List<PostOverviewDTO> posts = postService.convertPostsToOverviews(postService.getAllPostsOrderedByRating());
        result.put("posts", posts);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostDetails(@PathVariable Long postId) {
        postService.calculateAllRatings();
        Post post = postService.getPostById(postId);

        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<Post> addNewPost(@RequestBody @Valid NewPostDTO postData,
                                           @AuthenticationPrincipal User user) {
        Post newPost = postService.create(postData, user);

        return ResponseEntity.ok(newPost);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Post> editPost(@RequestBody @Valid NewPostDTO postData,
                                         @PathVariable Long postId,
                                         @AuthenticationPrincipal User user) {
        Post originalPost = postService.getPostById(postId);
        postService.verifyAuthor(originalPost, user);
        Post updatedPost = postService.update(originalPost, postData);

        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map> deletePost(@PathVariable Long postId,
                                          @AuthenticationPrincipal User user) {
        Map<String, String> result = new HashMap<>();
        Post post = postService.getPostById(postId);
        postService.verifyAuthor(post, user);
        postService.softDelete(post);
        result.put("message", "Post with ID " + postId + " has been successfully deleted.");

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{postId}/vote-up")
    public ResponseEntity<Map> upvotePost(@PathVariable Long postId,
                                          @AuthenticationPrincipal User user) {
        Map<String, Integer> result = new HashMap<>();
        Post post = postService.getPostById(postId);
        int newRating = postService.voteUp(post, user);
        result.put("rating", newRating);

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{postId}/vote-down")
    public ResponseEntity<Map> downvotePost(@PathVariable Long postId,
                                          @AuthenticationPrincipal User user) {
        Map<String, Integer> result = new HashMap<>();
        Post post = postService.getPostById(postId);
        int newRating = postService.voteDown(post, user);
        result.put("rating", newRating);

        return ResponseEntity.ok(result);
    }
}
