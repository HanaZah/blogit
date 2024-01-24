package com.blog.blogbackend.unit;

import com.blog.blogbackend.exceptions.IllegalVoteException;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.models.Vote;
import com.blog.blogbackend.repositories.VoteRepository;
import com.blog.blogbackend.services.VoteService;
import com.blog.blogbackend.services.VoteServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class VoteServiceUnitTests {

    private VoteService voteService;
    @Mock
    private VoteRepository voteRepository;
    private User author;
    private Post post;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        voteService = new VoteServiceImpl(voteRepository);
        author = new User("johndoe", "password");
        post = new Post("Test title", "test content", author);
        post.setId(1L);
    }

    @Test
    public void calculateRatingForPostReturnsPostWithCorrectRating() {
        Vote onlyVote = new Vote(author, post);
        onlyVote.setVoteValue(1);

        when(voteRepository.getRatingForPost(post)).thenReturn(onlyVote.getVoteValue());

        post = voteService.calculateRatingForPost(post);

        assertEquals(onlyVote.getVoteValue(), post.getRating());
    }

    @Test
    public void softDeleteVoteSetsDeletedToTrue() {
        Vote vote = new Vote(author, post);
        vote.setVoteValue(1);

        when(voteRepository.save(vote)).thenReturn(vote);

        voteService.softDelete(vote);

        verify(voteRepository).save(vote);
        assertEquals(true, vote.isDeleted());
    }

    @Test
    public void voteUpReturnsPostWithRatingOneUpWhenNotVotedBefore() {
        User newUser = new User("johnynew", "password");
        List<Vote> votes = new ArrayList<>();

        when(voteRepository.findByUserAndPost(newUser, post)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote vote = invocation.getArgument(0);
            votes.add(vote);
            post.setVotes(votes);
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(1);

        post = voteService.voteUp(post, author);

        verify(voteRepository).save(any(Vote.class));
        assertEquals(1, post.getRating());
        assertEquals(1, post.getVotes().size());
        assertEquals(1, post.getVotes().get(0).getVoteValue());
    }

    @Test
    public void voteUpReturnsPostWithRatingOneUpWhenDownvotedBefore() {
        List<Vote> votes = new ArrayList<>();
        Vote vote = new Vote(author, post);
        vote.setVoteValue(-1);
        votes.add(vote);
        post.setVotes(votes);

        when(voteRepository.findByUserAndPost(author, post)).thenReturn(Optional.of(vote));
        when(voteRepository.save(vote)).thenAnswer(invocation -> {
            votes.set(0, vote);
            post.setVotes(votes);
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(0);

        post = voteService.voteUp(post, author);

        verify(voteRepository).save(any(Vote.class));
        assertEquals(0, post.getRating());
        assertEquals(1, post.getVotes().size());
        assertEquals(0,post.getVotes().get(0).getVoteValue());
    }

    @Test
    public void voteUpThrowsIllegalVoteExceptionWhenUpvotedAlready() {
        List<Vote> votes = new ArrayList<>();
        Vote vote = new Vote(author, post);
        vote.setVoteValue(1);
        votes.add(vote);
        post.setVotes(votes);

        when(voteRepository.findByUserAndPost(author, post)).thenReturn(Optional.of(vote));
        when(voteRepository.save(vote)).thenAnswer(invocation -> {
            if(vote.getVoteValue() > 1) {
                throw new ConstraintViolationException("Vote value cannot exceed 1.", null);
            }
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(1);

        assertThrows(IllegalVoteException.class, () -> voteService.voteUp(post, author));
    }

    @Test
    public void voteDownReturnsPostWithRatingOneUpWhenNotVotedBefore() {
        User newUser = new User("johnynew", "password");
        List<Vote> votes = new ArrayList<>();

        when(voteRepository.findByUserAndPost(newUser, post)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote vote = invocation.getArgument(0);
            votes.add(vote);
            post.setVotes(votes);
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(-1);

        post = voteService.voteDown(post, author);

        verify(voteRepository).save(any(Vote.class));
        assertEquals(-1, post.getRating());
        assertEquals(1, post.getVotes().size());
        assertEquals(-1, post.getVotes().get(0).getVoteValue());
    }

    @Test
    public void voteDownReturnsPostWithRatingOneUpWhenUpvotedBefore() {
        List<Vote> votes = new ArrayList<>();
        Vote vote = new Vote(author, post);
        vote.setVoteValue(1);
        votes.add(vote);
        post.setVotes(votes);

        when(voteRepository.findByUserAndPost(author, post)).thenReturn(Optional.of(vote));
        when(voteRepository.save(vote)).thenAnswer(invocation -> {
            votes.set(0, vote);
            post.setVotes(votes);
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(0);

        post = voteService.voteDown(post, author);

        verify(voteRepository).save(any(Vote.class));
        assertEquals(0, post.getRating());
        assertEquals(1, post.getVotes().size());
        assertEquals(0,post.getVotes().get(0).getVoteValue());
    }

    @Test
    public void voteDownThrowsIllegalVoteExceptionWhenDownvotedAlready() {
        List<Vote> votes = new ArrayList<>();
        Vote vote = new Vote(author, post);
        vote.setVoteValue(-1);
        votes.add(vote);
        post.setVotes(votes);

        when(voteRepository.findByUserAndPost(author, post)).thenReturn(Optional.of(vote));
        when(voteRepository.save(vote)).thenAnswer(invocation -> {
            if(vote.getVoteValue() < 1) {
                throw new ConstraintViolationException("Vote value cannot be below -1.", null);
            }
            return vote;
        });
        when(voteRepository.getRatingForPost(post)).thenReturn(-1);

        assertThrows(IllegalVoteException.class, () -> voteService.voteDown(post, author));
    }
}
