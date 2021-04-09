package com.altruist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOperationException extends RuntimeException{

    public InvalidOperationException(String message) {
        super(message);
    }
}
