package com.itforshort.blog_spring_postgre_react.repository;

import com.itforshort.blog_spring_postgre_react.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPublished(boolean published, Pageable pageable);
    Page<Post> findByTitleContaining(String title, Pageable pageable);
}
