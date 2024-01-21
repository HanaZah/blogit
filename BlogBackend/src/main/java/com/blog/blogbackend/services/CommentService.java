package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Comment;

public interface CommentService {
    void softDeleteComment(Comment comment);
}
