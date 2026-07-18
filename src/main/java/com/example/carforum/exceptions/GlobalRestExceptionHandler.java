package com.example.carforum.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler
    public String handleAuthenticationException(AuthenticationException e){
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler
    public String handleAuthorizationException(AuthorizationException e){
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }


}
