package com.singtel.interviewtest.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

@Service
@Slf4j
public class HttpRequester {
    private static final String QUERY_STRING_DELIMITER = "?";

    @Retryable(value = {Exception.class}, maxAttempts = 2, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String executeSendPost(String urlStr, String payload) throws IOException {
        log.debug(MessageFormat.format("Sending to {0} with payload: {1}", urlStr, payload != null));

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod(HttpMethod.POST.toString());
        conn.connect();

        if (StringUtils.isNotBlank(payload)) {
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),
                    StandardCharsets.UTF_8)) {
                if (StringUtils.isNoneBlank(payload)) {
                    out.write(payload);
                    out.flush();
                }
            }
        }

        return processConnectOutput(conn);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 2, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String executeSendGet(String urlStr, String queryString) throws IOException {
        log.debug(MessageFormat.format("Sending to {0} with {1}", urlStr, queryString));

        URL url;
        if (StringUtils.isNotBlank(queryString)) {
            url = new URL(String.join(QUERY_STRING_DELIMITER, urlStr, queryString));
        } else {
            url = new URL(urlStr);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod(HttpMethod.GET.toString());
        conn.connect();
        return processConnectOutput(conn);
    }

    private String processConnectOutput(HttpURLConnection conn) throws IOException {
        InputStream is;
        int code = conn.getResponseCode();
        if (code == 200) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        StringBuilder result = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        }

        String resultStr = result.toString();
        log.debug(MessageFormat.format("Response: {0}", resultStr));
        return resultStr;
    }
}
