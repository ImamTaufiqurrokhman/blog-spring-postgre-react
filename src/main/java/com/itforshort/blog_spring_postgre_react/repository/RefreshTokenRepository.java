package com.itforshort.blog_spring_postgre_react.repository;

import com.itforshort.blog_spring_postgre_react.model.RefreshToken;
import com.itforshort.blog_spring_postgre_react.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
//    @Override
//    Optional<RefreshToken> findById(Long id);
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);

    @Modifying
    int deleteByUserId(long user_id);
}
