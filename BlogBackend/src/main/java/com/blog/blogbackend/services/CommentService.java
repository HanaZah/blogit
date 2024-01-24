package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;

import java.util.List;

public interface CommentService {
    List<Comment> getAllComments();

    Comment getCommentById(Long id);

    List<Comment> getCommentsForPost(Post post);

    Comment create(String content, User user, Post post);

    Comment update(Comment comment, String newContent);

    void verifyAuthor(Comment comment, User user);

    void softDelete(Comment comment);
}
