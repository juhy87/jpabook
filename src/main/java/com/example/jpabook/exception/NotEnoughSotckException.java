package com.example.jpabook.exception;

public class NotEnoughSotckException extends RuntimeException {

    public NotEnoughSotckException() {
        super();
    }

    public NotEnoughSotckException(String message) {
        super(message);
    }

    public NotEnoughSotckException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughSotckException(Throwable cause) {
        super(cause);
    }

}
