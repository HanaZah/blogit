package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> getAllComments() {
        return commentRepository.findAllByDeletedOrderByCreatedAtDesc(false);
    }

    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.findByIdAndDeleted(id, false).orElseThrow(
                () -> new EntityNotFoundException("Comment with ID " + id + "does not exist."));
    }

    @Override
    public List<Comment> getCommentsForPost(Post post) {
        return commentRepository.findAllByPostAndDeleted(post, false);
    }

    @Override
    public Comment create(String content, User user, Post post) {
        Comment comment = new Comment(content, user, post);
        return commentRepository.save(comment);
    }

    @Override
    public Comment update(Comment comment, String newContent) {
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    @Override
    public void verifyAuthor(Comment comment, User user) {
        if(!comment.getAuthor().equals(user)) {
            throw new AccessDeniedException("Only the author can edit this comment.");
        }
    }

    @Override
    public void softDelete(Comment comment) {
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
