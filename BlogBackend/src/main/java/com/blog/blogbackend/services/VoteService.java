package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.Vote;

public interface VoteService {
    Post calculateRatingForPost(Post post);

    void deleteVote(Vote vote);
}
