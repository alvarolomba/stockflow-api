package com.stockflow.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.stockflow.api.users.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES))
                .sign(algorithm());
    }

    public Long extractUserId(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm()).build();
            String subject = verifier.verify(token).getSubject();
            return Long.valueOf(subject);
        } catch (JWTVerificationException | NumberFormatException ex) {
            return null;
        }
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(properties.secret());
    }
}

