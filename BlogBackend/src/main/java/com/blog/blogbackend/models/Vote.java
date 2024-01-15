package com.blog.blogbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "post_votes")
public class Vote {

    @EmbeddedId
    private VoteId id = new VoteId();
    @Min(-1)
    @Max(1)
    private int value = 0;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    private boolean deleted = false;

    public Vote(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public Vote() {
    }

    public VoteId getId() {
        return id;
    }

    public void setId(VoteId id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int vote) {
        this.value = vote;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
