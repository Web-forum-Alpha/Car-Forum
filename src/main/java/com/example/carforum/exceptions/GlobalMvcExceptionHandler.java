package com.example.carforum.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalMvcExceptionHandler {

    @ExceptionHandler
    public String handleAuthenticationException(AuthenticationException e){

        return "redirect:/users/login";
    }

    @ExceptionHandler
    public String handleEntityNoFoundException(EntityNotFoundException e, Model model){

        model.addAttribute("error", e.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
        return "ErrorView";

    }
    @ExceptionHandler
    public String handleAuthorizationException(AuthorizationException e, Model model){

        model.addAttribute("error", e.getMessage());
        model.addAttribute("statusCode", HttpStatus.NOT_FOUND.getReasonPhrase());
        return "ErrorView";

    }
}
