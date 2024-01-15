package com.blog.blogbackend.models;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class VoteId implements Serializable {

    private Long userId;
    private Long postId;

    public VoteId() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
