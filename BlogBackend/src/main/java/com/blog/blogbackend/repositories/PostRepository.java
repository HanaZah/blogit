package com.blog.blogbackend.repositories;

import com.blog.blogbackend.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByDeletedOrderByRatingDesc(boolean deleted);

    Optional<Post> findByTitleAndDeleted(String title, boolean deleted);

    Optional<Post> findByIdAndDeleted(long id, boolean deleted);
}
