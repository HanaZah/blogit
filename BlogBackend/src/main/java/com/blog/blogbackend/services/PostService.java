package com.blog.blogbackend.services;

import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;

import java.util.List;

public interface PostService {
    void calculateAllRatings();

    List<Post> getAllPostsOrderedByRating();

    List<PostOverviewDTO> convertPostsToOverviews(List<Post> posts);

    Post createPost(NewPostDTO postData, User author);

    Post getPostById(Long id);

    Post updatePostByAuthor(Post originalPost, NewPostDTO updateData);

    void verifyAuthor(Post post, User author);

    void deletePost(Post post);
}
