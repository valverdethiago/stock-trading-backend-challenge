package com.altruist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
public class InvalidTradeStatusException extends RuntimeException{

    public InvalidTradeStatusException(String message) {
        super(message);
    }
}
