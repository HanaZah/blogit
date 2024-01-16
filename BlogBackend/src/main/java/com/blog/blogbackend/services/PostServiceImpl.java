package com.blog.blogbackend.services;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.repositories.PostRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.InputMismatchException;
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
    public Post createPost(NewPostDTO postData, User author) {
        Post post = new Post(postData.getTitle(), postData.getContent(), author);

        return postRepository.save(post);
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new InputMismatchException("Post ID does not match."));
    }

    @Override
    public void verifyAuthor(Post post, User author) {
        if(!post.getAuthor().equals(author)) {
            throw new AccessDeniedException("Only the author can edit this post.");
        }
    }

    @Override
    public Post updatePost(Post originalPost, NewPostDTO updateData) {

        originalPost.setTitle(updateData.getTitle());
        originalPost.setContent(updateData.getContent());

        return postRepository.save(originalPost);
    }

    @Override
    public void deletePost(Post post) {
        post.setDeleted(true);
        post.getComments().forEach(commentService::deleteComment);
        post.getVotes().forEach(voteService::deleteVote);

        postRepository.save(post);
    }

}
