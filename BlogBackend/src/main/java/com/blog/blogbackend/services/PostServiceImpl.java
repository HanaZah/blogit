package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final VoteService voteService;
    private final CommentService commentService;

    public PostServiceImpl(PostRepository postRepository, VoteService voteService, CommentService commentService) {
        this.postRepository = postRepository;
        this.voteService = voteService;
        this.commentService = commentService;
    }

    @Override
    public void calculateAllRatings() {
        List<Post> posts = postRepository.findAllByDeletedOrderByRatingDesc(false);
        posts.forEach(post -> postRepository.save(voteService.calculateRatingForPost(post)));
    }

    @Override
    public List<Post> getAllPostsOrderedByRating() {
        calculateAllRatings();
        return postRepository.findAllByDeletedOrderByRatingDesc(false);
    }

    @Override
    public List<PostOverviewDTO> convertPostsToOverviews(List<Post> posts) {
        List<PostOverviewDTO> overviews = new ArrayList<>();
        posts.forEach(post -> overviews.add(new PostOverviewDTO(post)));
        return overviews;
    }

    @Override
    public Post create(NewPostDTO postData, User author) {
        Post post = new Post(postData.getTitle(), postData.getContent(), author);

        return postRepository.save(post);
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new EntityNotFoundException("Post with ID" + id + "does not exist."));
    }

    @Override
    public void verifyAuthor(Post post, User author) {
        if(!post.getAuthor().equals(author)) {
            throw new AccessDeniedException("Only the author can edit this post.");
        }
    }

    @Override
    public Post update(Post originalPost, NewPostDTO updateData) {
        originalPost.setTitle(updateData.getTitle());
        originalPost.setContent(updateData.getContent());

        return postRepository.save(originalPost);
    }

    @Override
    public void softDelete(Post post) {
        post.setDeleted(true);
        post.getComments().forEach(commentService::softDelete);
        post.getVotes().forEach(voteService::softDelete);

        postRepository.save(post);
    }

    @Override
    public int voteUp(Post post, User user) {
        return postRepository
                .save(voteService.voteUp(post, user))
                .getRating();
    }

    @Override
    public int voteDown(Post post, User user) {
        return postRepository
                .save(voteService.voteDown(post, user))
                .getRating();
    }

}
