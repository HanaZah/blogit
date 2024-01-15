package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.repositories.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void deleteComment(Comment comment) {
        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
