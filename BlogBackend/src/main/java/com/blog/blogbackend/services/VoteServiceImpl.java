package com.blog.blogbackend.services;

import com.blog.blogbackend.exceptions.IllegalVoteException;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
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
    public void softDelete(Vote vote) {
        vote.setDeleted(true);
        voteRepository.save(vote);
    }

    @Override
    public Post voteUp(Post post, User votingUser) {
        Vote vote = voteRepository.findByUserAndPost(votingUser, post).orElse(new Vote(votingUser, post));

        try {
            vote.setVoteValue(vote.getVoteValue() + 1);
            voteRepository.save(vote);
            return calculateRatingForPost(post);
        }catch (Exception e){
            throw new IllegalVoteException("You have already upvoted this post.");
        }
    }

    @Override
    public Post voteDown(Post post, User votingUser) {
        Vote vote = voteRepository.findByUserAndPost(votingUser, post).orElse(new Vote(votingUser, post));

        try {
            vote.setVoteValue(vote.getVoteValue() - 1);
            voteRepository.save(vote);
            return calculateRatingForPost(post);
        }catch (Exception e){
            throw new IllegalVoteException("You have already downvoted this post.");
        }
    }
}
