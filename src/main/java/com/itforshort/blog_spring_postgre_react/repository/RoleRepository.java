package com.itforshort.blog_spring_postgre_react.repository;

import com.itforshort.blog_spring_postgre_react.model.ERole;
import com.itforshort.blog_spring_postgre_react.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
