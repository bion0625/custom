package com.self_true.exception;

public class NotFoundMemberException extends RuntimeException{
    public NotFoundMemberException(String message) {
        super(message);
    }
}
