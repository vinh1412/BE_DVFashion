/*
 * @ {#} JwtUtils.java   1.0     15/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.utils.AESUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
 * @description: Utility class for handling JWT operations
 * @author: Tran Hien Vinh
 * @date:   15/08/2025
 * @version:    1.0
 */
@Component
public class JwtUtils {
    @Value("${jwt.signed-key}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    @Value("${jwt.secret-key}")   // Key riêng cho AES
    private String aesSecret;

    private AESUtils aesUtils;

    @PostConstruct
    public void init() {
        this.aesUtils = new AESUtils(aesSecret);
    }

    // Creates a key from the JWT secret for signing and verifying tokens
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Generates an access token for the user
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    // Generates a refresh token for the user
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(claims, userDetails.getUsername(), refreshTokenDurationMs);
    }

    // Generates a token with additional claims, subject, and expiration time
    private String generateToken(Map<String, Object> extraClaims, String subject, long expiration) {
        String jwt = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        // Encrypt JWT token using AES
        return aesUtils.encrypt(jwt);
    }

    // Extracts the username from the JWT token
    public String getUsernameFromJwtToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts the expiration date from the JWT token
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extracts a specific claim from the JWT token using a claims resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extracts all claims from the JWT token
    private Claims extractAllClaims(String token) {
        String decryptedToken = aesUtils.decrypt(token);
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(decryptedToken)
                .getBody();
    }

    // Checks if the token is expired
    private boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public boolean validateJwtToken(String authToken, UserDetails userDetails) {
        final String username = getUsernameFromJwtToken(authToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(authToken));
    }

    // Validates the JWT token and handles exceptions
    public boolean validateJwtToken(String encryptedToken) {
        try {
            String decryptedToken = aesUtils.decrypt(encryptedToken);
            Jwts.parserBuilder().setSigningKey(key()).build().parse(decryptedToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    // Calculates the maximum age of the access token
    public int getTokenMaxAge(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return (int) ((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
    }
}
