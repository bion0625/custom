package com.self_true.exception;

public class DuplicateMemberIdException extends RuntimeException{
    public DuplicateMemberIdException(String message) {
        super(message);
    }
}
