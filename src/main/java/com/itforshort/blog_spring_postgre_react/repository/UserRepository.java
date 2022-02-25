package com.itforshort.blog_spring_postgre_react.repository;

import com.itforshort.blog_spring_postgre_react.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String usernmae);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
//    Page<User> findByActive(boolean active, Pageable pageable);
    List<User> findByUsernameContaining(String name);
}
