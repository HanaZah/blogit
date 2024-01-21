package com.blog.blogbackend.exceptions;

public class IllegalVoteException extends RuntimeException {
    public IllegalVoteException(String message) {
        super(message);
    }
}
