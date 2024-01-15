package com.blog.blogbackend.services;

import com.blog.blogbackend.repositories.VoteRepository;
import org.springframework.stereotype.Service;

@Service
public class VoteServiceImpl implements VoteService{

    private final VoteRepository voteRepository;

    public VoteServiceImpl(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }
}
