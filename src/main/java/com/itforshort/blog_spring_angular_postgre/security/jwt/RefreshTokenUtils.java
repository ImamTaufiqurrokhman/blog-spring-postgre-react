package com.itforshort.blog_spring_angular_postgre.security.jwt;

import com.itforshort.blog_spring_angular_postgre.model.RefreshToken;
import com.itforshort.blog_spring_angular_postgre.model.User;
import com.itforshort.blog_spring_angular_postgre.repository.RefreshTokenRepository;
import com.itforshort.blog_spring_angular_postgre.repository.UserRepository;
import com.itforshort.blog_spring_angular_postgre.security.exception.TokenRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenUtils {

    private final RefreshTokenRepository refreshTokenRepository;

    private  final UserRepository userRepository;

    public RefreshTokenUtils(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenUtils.class);

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public ResponseCookie cleanRefreshToken() {
        return ResponseCookie
                .from("refreshToken", null)
                .path("/api")
                .build();
    }

    public ResponseCookie createRefreshToken(Long userId) {
        Optional<User> _user = userRepository.findById(userId);
        String _refreshToken = generateRefreshToken();
        RefreshToken refreshToken = new RefreshToken();
        long refreshTokenDurationMs = 2592000000L;
        refreshToken.setUser(_user.get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(_refreshToken);
        refreshToken = refreshTokenRepository.save(refreshToken);
        return ResponseCookie
                .from("refreshToken", _refreshToken)
                .path("/api")
                .maxAge(24 * 60 * 60 * 30)
                .httpOnly(true)
                .build();
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(refreshToken.getToken(), "Refresh token was expired.");
        }
        return refreshToken;
    }

    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
