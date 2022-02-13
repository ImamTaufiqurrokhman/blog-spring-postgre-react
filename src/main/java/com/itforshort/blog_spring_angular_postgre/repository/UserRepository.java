package com.itforshort.blog_spring_angular_postgre.repository;

import com.itforshort.blog_spring_angular_postgre.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByActive(boolean active);
    List<User> findByNameContaining(String name);
}
