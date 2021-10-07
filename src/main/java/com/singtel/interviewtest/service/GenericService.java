package com.singtel.interviewtest.service;

import com.singtel.interviewtest.exception.InvalidJwtException;
import com.singtel.interviewtest.exception.SingtelException;

public interface GenericService {
    String performInternalGet(String oldPath, String queryString, String jwtToken, String requestTime) throws SingtelException, InvalidJwtException;

    String performInternalPost(String oldPath, String payload, String jwtToken, String requestTime) throws SingtelException, InvalidJwtException;
}
