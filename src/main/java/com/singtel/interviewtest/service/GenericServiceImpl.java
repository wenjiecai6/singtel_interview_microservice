package com.singtel.interviewtest.service;

import com.singtel.interviewtest.constants.Constants;
import com.singtel.interviewtest.entity.RequestTrace;
import com.singtel.interviewtest.exception.InvalidJwtException;
import com.singtel.interviewtest.exception.SingtelException;
import com.singtel.interviewtest.repository.RequestTraceRepository;
import com.singtel.interviewtest.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;

import static javax.transaction.Transactional.TxType.REQUIRED;

@Service
@Slf4j
public class GenericServiceImpl implements GenericService {

    @Autowired
    private HttpRequester httpRequester;

    @Autowired
    private RequestTraceRepository requestTraceRepository;

    @Value("${internal.path.regex:https://{0}}")
    private String internalPathPrefix;

    @Value("${generic.path.prefix:/api}")
    private String genericPathPrefix;

    @Value("${generic.exception.message:https:Generic exception: {0}}")
    private String genericExceptionMessage;

    @Value("${jwt.secret:abc}")
    private String jwtSecret;

    @Value("${jwt.grace.period.min:5}")
    private int jwtGracePeriodMin;

    @Override
    @Transactional(REQUIRED)
    public String performInternalGet(String oldPath, String queryString, String jwtToken, String requestTime) throws SingtelException, InvalidJwtException {
        log.debug(MessageFormat.format("performInternalGet: {0}", oldPath));
        JwtUtil.validateJwt(jwtToken, oldPath, queryString, requestTime, jwtSecret);

        String newPath = processPath(oldPath);
        String result;
        try {
            requestTraceRepository.save(new RequestTrace(oldPath));
            result = httpRequester.executeSendGet(newPath, queryString);
        } catch (IOException ioe) {
            String uuid = UUID.randomUUID().toString();
            log.error(uuid, ioe);
            //not to expose the error from internal api
            throw new SingtelException(MessageFormat.format(genericExceptionMessage, uuid));
        }
        return result;
    }

    @Override
    @Transactional(REQUIRED)
    public String performInternalPost(String oldPath, String payload, String jwtToken, String requestTime) throws SingtelException, InvalidJwtException {
        log.debug(MessageFormat.format("performInternalPost: {0}", oldPath));
        JwtUtil.validateJwt(jwtToken, oldPath, payload, requestTime, jwtSecret);

        String newPath = processPath(oldPath);
        String result;
        try {
            requestTraceRepository.save(new RequestTrace(oldPath));
            result = httpRequester.executeSendPost(newPath, payload);
        } catch (IOException ioe) {
            String uuid = UUID.randomUUID().toString();
            log.error(uuid, ioe);
            //not to expose the error from internal api
            throw new SingtelException(MessageFormat.format(genericExceptionMessage, uuid));
        }
        return result;
    }

    private String processPath(String oldPath) throws SingtelException {
        String newPath = StringUtils.removeStart(oldPath, genericPathPrefix);
        if (newPath.startsWith(Constants.BACK_SLASH)) {//this is to cater for oldPath = /api
            newPath = StringUtils.removeStart(newPath, Constants.BACK_SLASH);
        }

        if (StringUtils.isBlank(newPath)) {
            throw new SingtelException(MessageFormat.format("Invalid path: {0}", oldPath));
        }

        String newFullPath = MessageFormat.format(internalPathPrefix, newPath);
        log.debug(MessageFormat.format("New FullPath: {0}", newFullPath));

        return newFullPath;
    }
}
