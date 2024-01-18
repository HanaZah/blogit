package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.Vote;
import com.blog.blogbackend.repositories.VoteRepository;
import org.springframework.stereotype.Service;

@Service
public class VoteServiceImpl implements VoteService{

    private final VoteRepository voteRepository;

    public VoteServiceImpl(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @Override
    public Post calculateRatingForPost(Post post) {
        int rating = voteRepository.getRatingForPost(post);
        post.setRating(rating);
        return post;
    }

    @Override
    public void deleteVote(Vote vote) {
        vote.setDeleted(true);
        voteRepository.save(vote);
    }
}
