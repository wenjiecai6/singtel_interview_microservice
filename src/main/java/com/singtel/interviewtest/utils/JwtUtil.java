package com.singtel.interviewtest.utils;

import com.singtel.interviewtest.constants.Constants;
import com.singtel.interviewtest.exception.InvalidJwtException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class JwtUtil {

    public static String createJWT(String appSecret, String requestTime, String uri, String queryString, int expirationSec) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Key signingKey = new SecretKeySpec(appSecret.getBytes(), signatureAlgorithm.getJcaName());

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.REQUEST_TIME, requestTime);
        claims.put(Constants.URI, uri);
        claims.put(Constants.QUERY_STRING, queryString);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setHeaderParam("typ", "JWT")
                .signWith(signatureAlgorithm, signingKey)
                .setExpiration(DateUtils.addSeconds(new Date(), expirationSec));

        return builder.compact();
    }

    public static void validateJwt(String jwtToken, String requestURI, String payload, String requestTime, String jwtSecret) throws InvalidJwtException {
        if (StringUtils.isBlank(jwtToken)) {
            throw new InvalidJwtException("Missing JWT Token");
        } else {
            Claims claims;
            try {
                claims = Jwts.parser()
                        .setSigningKey(jwtSecret.getBytes())
                        .parseClaimsJws(jwtToken).getBody();

            } catch (ExpiredJwtException eje) {
                String uuid = UUID.randomUUID().toString();
                log.error(uuid, eje);
                //not to expose the error from internal api
                throw new InvalidJwtException(MessageFormat.format("Expired JWT", uuid));
            }

            if (claims == null) {
                throw new InvalidJwtException("Invalid JWT, empty claims");
            } else {
                if (!(equals(requestTime, claims.get(Constants.REQUEST_TIME)) && equals(requestURI, claims.get(Constants.URI)) && equals(payload, claims.get(Constants.QUERY_STRING)))) {
                    throw new InvalidJwtException("Invalid JWT");
                }
            }
        }
    }

    private static boolean equals(String a, Object b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null) {
            return false;
        } else if (b == null) {
            return false;
        } else {
            return (a.equals(b.toString()));
        }
    }
}
