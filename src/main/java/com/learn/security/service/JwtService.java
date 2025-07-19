package com.learn.security.service;

import com.learn.security.config.JwtConfig;
import com.learn.security.domain.RefreshToken;
import com.learn.security.domain.User;
import com.learn.security.enums.Role;
import com.learn.security.repository.RefreshTokenRepository;
import com.learn.security.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(User user) {
        return generateToken(user, jwtConfig.getAccessTokenExpiration());
    }

    public String generateRefreshToken(User user) {
        String refreshToken = generateToken(user, jwtConfig.getRefreshTokenExpiration());
        saveRefreshToken(refreshToken, user.getId());
        return refreshToken;
    }

    private void saveRefreshToken(String token, Long userId) {

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElse(new RefreshToken());

        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setIsValid(true);
        var currentDate = new Date();
        refreshToken.setCreatedAt(currentDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND, jwtConfig.getRefreshTokenExpiration());

        refreshToken.setExpiryDate(calendar.getTime());
        refreshTokenRepository.save(refreshToken);
    }

    private String generateToken(User user, long tokenExpiration) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .signWith(jwtConfig.getSecretKey())
                .compact();
    }

    public boolean isTokenExpired(String token) {
        try {
            var claims = getClaims(token);

            RefreshToken refreshToken = refreshTokenRepository.findByUserId(Long.valueOf(claims.getSubject()))
                    .orElse(null);

            return (nonNull(refreshToken) && !refreshToken.getIsValid()) || claims.getExpiration().before(new Date());
        }
        catch (JwtException ex) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        token = token.replace("Bearer ", "");
        return Long.valueOf(getClaims(token).getSubject());
    }

    public Role getRoleFromToken(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

    public void logout(Long userId) {

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElse(null);

        if (nonNull(refreshToken)) {
            refreshToken.setIsValid(false);
            refreshToken.setExpiryDate(new Date());
            refreshTokenRepository.save(refreshToken);
        }
    }
}
