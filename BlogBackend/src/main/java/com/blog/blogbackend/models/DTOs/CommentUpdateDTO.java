package com.blog.blogbackend.models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentUpdateDTO {

    @NotBlank(message = "Content is required.")
    @NotNull(message = "Content is required.")
    private String content;

    public CommentUpdateDTO() {
    }

    public CommentUpdateDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
