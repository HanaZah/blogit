package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.models.Vote;

public interface VoteService {
    Post calculateRatingForPost(Post post);

    void softDeleteVote(Vote vote);

    Post voteUp(Post post, User votingUser);

    Post voteDown(Post post, User votingUser);
}
