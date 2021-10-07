package com.singtel.interviewtest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidJwtException extends Exception {
    public InvalidJwtException(String message) {
        super(message);
    }
}
