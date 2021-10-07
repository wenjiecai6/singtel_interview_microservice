package com.singtel.interviewtest.controller;

import com.singtel.interviewtest.constants.Constants;
import com.singtel.interviewtest.entity.RequestTrace;
import com.singtel.interviewtest.repository.RequestTraceRepository;
import com.singtel.interviewtest.service.HttpRequester;
import com.singtel.interviewtest.utils.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class GenericControllerTests {

    protected MockMvc mvc;

    @MockBean
    HttpRequester httpRequester;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RequestTraceRepository requestTraceRepository;

    @Value("${jwt.secret:abc}")
    private String jwtSecret;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testService1() throws Exception {
        Mockito.when(httpRequester.executeSendGet(any(), any())).thenReturn("Done");
        int currentDbSize = requestTraceRepository.findAll().size();

        String uri = "/api/service1/test1";
        String queryString = "param1=1";
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri, queryString, 300);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(String.join(Constants.QUESTION_MARK, uri, queryString))
                .header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).andReturn();

        verify(httpRequester).executeSendGet("https://service1/test1", "param1=1");

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        //check database
        assertEquals(currentDbSize + 1, requestTraceRepository.findAll().size());
        Optional<RequestTrace> lastRequestTrace = requestTraceRepository.findById((long) (currentDbSize + 1));
        assertTrue(lastRequestTrace.isPresent());
        assertEquals(uri, lastRequestTrace.get().getUrl());
    }

    @Test
    public void testService2() throws Exception {
        Mockito.when(httpRequester.executeSendPost(any(), any())).thenReturn("Done");
        int currentDbSize = requestTraceRepository.findAll().size();

        String uri = "/api/service2/test2";
        String payload = "{\"name\":\"testing2\"}";
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri, payload, 300);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).content(payload).header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).
                andReturn();

        verify(httpRequester).executeSendPost("https://service2/test2", payload);

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        //check database
        assertEquals(currentDbSize + 1, requestTraceRepository.findAll().size());
        Optional<RequestTrace> lastRequestTrace = requestTraceRepository.findById((long) (currentDbSize + 1));
        assertTrue(lastRequestTrace.isPresent());
        assertEquals(uri, lastRequestTrace.get().getUrl());
    }

    @Test
    public void testService3() throws Exception {
        Mockito.when(httpRequester.executeSendPost(any(), any())).thenReturn("Done");
        int currentDbSize = requestTraceRepository.findAll().size();

        String uri = "/api/service3/test3/misc";
        String payload = "{\"name\":\"testing3\"}";
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri, payload, 300);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri).content(payload).header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).
                andReturn();

        verify(httpRequester).executeSendPost("https://service3/test3/misc", payload);

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        //check database
        assertEquals(currentDbSize + 1, requestTraceRepository.findAll().size());
        Optional<RequestTrace> lastRequestTrace = requestTraceRepository.findById((long) (currentDbSize + 1));
        assertTrue(lastRequestTrace.isPresent());
        assertEquals(uri, lastRequestTrace.get().getUrl());
    }

    @Test
    public void testServiceInvalidUri() throws Exception {
        String uri = "/api/";
        String queryString = null;
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri, queryString, 300);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status);
    }

    @Test
    public void testServiceFailJwtMissing() throws Exception {
        String uri = "/api/";

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status);
    }

    @Test
    public void testServiceFailJwtWrongValue() throws Exception {
        String uri = "/api/";
        String queryString = "";
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri + "a", queryString, 300);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status);
    }

    @Test
    public void testServiceFailJwtExpired() throws Exception {
        String uri = "/api/";
        String queryString = "";
        String requestTime = "2021/06/10 10:10:10";

        String jwtStr = JwtUtil.createJWT(jwtSecret, requestTime, uri + "a", queryString, 0);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .header(Constants.JWT_TOKEN, jwtStr).header(Constants.REQUEST_TIME, requestTime)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status);
    }
}
