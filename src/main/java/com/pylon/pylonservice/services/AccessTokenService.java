package com.pylon.pylonservice.services;

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
import org.springframework.stereotype.Service;

import static com.pylon.pylonservice.constants.TimeConstants.ONE_DAY_IN_MILLISECONDS;

@Log4j2
@Service
public class AccessTokenService {
    private final Key secretKey;

    AccessTokenService(@Value("${jwt.secret}") final String secretString) {
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isAccessTokenValid(final String token, final UserDetails userDetails) {
        return getUsernameFromAccessToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String getUsernameFromAccessToken(@NonNull final String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getUsernameFromAccessTokenOrDefaultIfNull(final String token, final String defaultUsername) {
        return token == null ? defaultUsername : getClaimFromToken(token, Claims::getSubject);
    }

    public String generateAccessTokenForUser(final UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + ONE_DAY_IN_MILLISECONDS))
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
