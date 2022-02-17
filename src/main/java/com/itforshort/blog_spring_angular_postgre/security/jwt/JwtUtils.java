package com.itforshort.blog_spring_angular_postgre.security.jwt;

import com.itforshort.blog_spring_angular_postgre.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//    @Value("${itforshort.app.jwtSecret}")
//    private String jwtSecret;
//    @Value("${itforshort.app.jwtCookieName}")
//    private String jwtCookie;
    @Value("${itforshort.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, "authToken");
        if (cookie != null) {
            return cookie.getValue();
        } else {
            final String token = UUID.randomUUID().toString();
            final CookieGenerator g = new CookieGenerator();
            g.setCookieMaxAge(Integer.MAX_VALUE);
            g.setCookiePath("/");
            g.setCookieName("authToken");
            g.addCookie(response, token);
            logger.debug("Generated a new token: {}", token);
            return token;
    }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return ResponseCookie
                .from("authToken", jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, "secretKey")
            .compact();
        }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie
                .from("authToken", null)
                .path("/api")
                .build();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey("secretKey")
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey("secretKey").parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
