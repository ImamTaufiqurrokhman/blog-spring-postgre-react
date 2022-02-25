package com.itforshort.blog_spring_postgre_react.controller;

import com.itforshort.blog_spring_postgre_react.model.ERole;
import com.itforshort.blog_spring_postgre_react.model.RefreshToken;
import com.itforshort.blog_spring_postgre_react.model.Role;
import com.itforshort.blog_spring_postgre_react.model.User;
import com.itforshort.blog_spring_postgre_react.payload.request.LoginRequest;
import com.itforshort.blog_spring_postgre_react.payload.request.SignupRequest;
import com.itforshort.blog_spring_postgre_react.payload.response.MessageResponse;
import com.itforshort.blog_spring_postgre_react.payload.response.UserInfoResponse;
import com.itforshort.blog_spring_postgre_react.repository.RefreshTokenRepository;
import com.itforshort.blog_spring_postgre_react.repository.RoleRepository;
import com.itforshort.blog_spring_postgre_react.repository.UserRepository;
import com.itforshort.blog_spring_postgre_react.security.exception.TokenRefreshException;
import com.itforshort.blog_spring_postgre_react.security.jwt.JwtUtils;
import com.itforshort.blog_spring_postgre_react.security.jwt.RefreshTokenUtils;
import com.itforshort.blog_spring_postgre_react.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public RoleRepository roleRepository;

    public PasswordEncoder encoder;

    public JwtUtils jwtUtils;

    public RefreshTokenUtils tokenUtils;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            RefreshTokenUtils tokenUtils
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.tokenUtils = tokenUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        ResponseCookie newRefreshToken = tokenUtils.createRefreshToken(userDetails.getId());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshToken.toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
          return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
          return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                             signUpRequest.getEmail(),
                             encoder.encode(signUpRequest.getPassword()),
                             true
                             );
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        } else {
          strRoles.forEach(role -> {
              switch (role) {
                  case "admin" -> {
                      Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      roles.add(adminRole);
                  }
                  case "mod" -> {
                      Role creRole = roleRepository.findByName(ERole.ROLE_CREATOR)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      roles.add(creRole);
                  }
                  default -> {
                      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      roles.add(userRole);
                  }
              }
          });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String _refreshToken = tokenUtils.getRefreshTokenFromCookies(request);
        Optional<RefreshToken> existedRefreshToken = refreshTokenRepository.findByToken(_refreshToken);
        existedRefreshToken.ifPresent(refreshToken -> refreshTokenRepository.deleteById(refreshToken.getId()));
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = tokenUtils.cleanRefreshToken();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String _refreshToken = tokenUtils.getRefreshTokenFromCookies(request);
        Optional<RefreshToken> existedRefreshToken = refreshTokenRepository.findByToken(_refreshToken);
        if (existedRefreshToken.isPresent()) {
            RefreshToken validRefreshToken = tokenUtils.verifyExpiration(existedRefreshToken.get());
            User user = validRefreshToken.getUser();
            try {
                refreshTokenRepository.deleteById(validRefreshToken.getId());
            } catch (Exception e) {
                logger.error("Failed to delete: {}", e.getMessage());
            }
            ResponseCookie newJwtCookie = jwtUtils.generateJwtCookieFromUser(user);
            ResponseCookie newRefreshToken = tokenUtils.createRefreshToken(user.getId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newJwtCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, newRefreshToken.toString())
                    .body(new MessageResponse("You've been refreshed!"));

        } else {
            throw new TokenRefreshException(_refreshToken, "Refresh token is invalid. Please login to continue!");
        }
    }
}
