package com.example.carforum.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EntityDuplicateException extends RuntimeException {
    public EntityDuplicateException(String message) {
        super(message);
    }
}
