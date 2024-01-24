package com.blog.blogbackend.utils;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TimeProvider {
    public TimeProvider() {}

    public Date now() {
        return new Date();
    }
}
