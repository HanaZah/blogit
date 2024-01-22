package com.blog.blogbackend.controllers;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.DTOs.CommentUpdateDTO;
import com.blog.blogbackend.models.DTOs.NewCommentDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.services.CommentService;
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
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;

    private final String defaultErrorMessage = "Post ID and comment content are required.";

    public CommentController(CommentService commentService, PostService postService) {
        this.commentService = commentService;
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
    public ResponseEntity<Map> getAllCommentsForPost(@RequestParam Long postId) {
        Map<String, List<Comment>> result = new HashMap<>();
        Post post = postService.getPostById(postId);
        List<Comment> comments = commentService.getCommentsForPost(post);
        result.put("comments", comments);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getComment(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);

        return ResponseEntity.ok(comment);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody @Valid NewCommentDTO commentData,
                                              @AuthenticationPrincipal User user) {
        Post post = postService.getPostById(commentData.getPostId());
        Comment comment = commentService.create(commentData.getContent(), user, post);

        return ResponseEntity.ok(comment);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long commentId,
                                                 @RequestBody @Valid CommentUpdateDTO updateData,
                                                 @AuthenticationPrincipal User user) {
        Comment originalComment = commentService.getCommentById(commentId);
        commentService.verifyAuthor(originalComment, user);
        Comment updatedComment = commentService.update(originalComment, updateData.getContent());

        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map> deleteComment(@PathVariable Long commentId,
                                             @AuthenticationPrincipal User user) {
        Map<String, String> result = new HashMap<>();
        Comment comment = commentService.getCommentById(commentId);
        commentService.verifyAuthor(comment, user);
        commentService.softDelete(comment);
        result.put("message", "Comment with ID " + commentId + " has been successfully deleted.");

        return ResponseEntity.ok(result);
    }
}
