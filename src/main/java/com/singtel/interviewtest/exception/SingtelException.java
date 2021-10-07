package com.singtel.interviewtest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SingtelException extends Exception {
    public SingtelException(String message) {
        super(message);
    }
}
