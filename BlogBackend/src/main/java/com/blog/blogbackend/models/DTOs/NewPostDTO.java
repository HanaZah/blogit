package com.blog.blogbackend.models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NewPostDTO {

    @NotNull(message = "Title is required.")
    @NotBlank(message = "Title is required.")
    private String title;
    @NotNull(message = "Content is required.")
    @NotBlank(message = "Content is required.")
    private String content;

    public NewPostDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public NewPostDTO() {
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
}
