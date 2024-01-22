package com.blog.blogbackend.repositories;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByDeletedOrderByCreatedAtDesc(boolean deleted);
    Optional<Comment> findByIdAndDeleted(Long id, boolean deleted);
    List<Comment> findAllByPostAndDeleted(Post post, boolean deleted);
}
