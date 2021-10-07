package com.singtel.interviewtest.controller;

import com.singtel.interviewtest.exception.InvalidJwtException;
import com.singtel.interviewtest.exception.SingtelException;
import com.singtel.interviewtest.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class GenericController {

    @Autowired
    private GenericService genericService;

    @GetMapping(value = "/**")
    public String genericGet(HttpServletRequest request, @RequestHeader("Jwt-Token") String jwtToken, @RequestHeader("Request-Time") String requestTime) throws SingtelException, InvalidJwtException {
        return genericService.performInternalGet(request.getRequestURI(), request.getQueryString(), jwtToken, requestTime);
    }

    @PostMapping(value = "/**")
    public String genericPost(HttpServletRequest request, @RequestBody String payload, @RequestHeader("Jwt-Token") String jwtToken, @RequestHeader("Request-Time") String requestTime) throws SingtelException, InvalidJwtException {
        return genericService.performInternalPost(request.getRequestURI(), payload, jwtToken, requestTime);
    }
}