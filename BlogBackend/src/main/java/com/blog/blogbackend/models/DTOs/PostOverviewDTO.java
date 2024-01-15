package com.blog.blogbackend.models.DTOs;

import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;

import java.util.Date;

public class PostOverviewDTO {
    private Long id;
    private String title;
    private String content;
    private Date createdAt;
    private int rating;
    private User author;
    private int numberOfComments;

    public PostOverviewDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.rating = post.getRating();
        this.author = post.getAuthor();
        numberOfComments = post.getComments().size();
    }

    public PostOverviewDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }
}
