package com.pylon.pylonservice.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class JwtTokenUtil {
    private static final long JWT_TOKEN_VALIDITY_MILLIS = 24 * 60 * 60 * 1000;
    private static final String BEARER_HEADER = "Bearer ";

    private final Key secretKey;

    JwtTokenUtil(@Value("${jwt.secret}") final String secretString) {
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public static String removeBearerFromAuthorizationHeader(final String header) {
        if (!header.startsWith(BEARER_HEADER)) {
            final String message =
                String.format(
                    "Authorization header value does not begin with '%s': %s",
                    BEARER_HEADER,
                    header
                );
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        return header.substring(7);
    }

    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        return getUsernameFromToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String getUsernameFromToken(@NonNull final String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String generateJwtForUser(final UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY_MILLIS))
            .signWith(secretKey)
            .compact();
    }

    private <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(final String jwt) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(jwt)
            .getBody();
    }

    private boolean isTokenExpired(final String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}
