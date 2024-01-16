package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.services.PostService;
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

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map> missingRequestBodyParts() {

        Map<String, String> result = new HashMap<>();
        result.put("error", "Title and text are required.");

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

        Post newPost = postService.createPost(postData, user);

        return ResponseEntity.ok(newPost);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Post> editPost(@RequestBody @Valid NewPostDTO postData,
                                         @PathVariable Long postId,
                                         @AuthenticationPrincipal User user) {

        Post originalPost = postService.getPostById(postId);
        postService.verifyAuthor(originalPost, user);
        Post updatedPost = postService.updatePost(originalPost, postData);

        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId,
                                             @AuthenticationPrincipal User user) {

        Post post = postService.getPostById(postId);
        postService.verifyAuthor(post, user);
        postService.deletePost(post);

        return ResponseEntity.ok("Post has been successfully deleted.");
    }
}
