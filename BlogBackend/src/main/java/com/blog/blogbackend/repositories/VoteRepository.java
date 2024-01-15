package com.blog.blogbackend.repositories;

import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndPost(User user, Post post);

    @Query("SELECT COALESCE(SUM(r.value), 0) FROM Vote r WHERE r.post = :post")
    int getRatingForPost(Post post);
}
