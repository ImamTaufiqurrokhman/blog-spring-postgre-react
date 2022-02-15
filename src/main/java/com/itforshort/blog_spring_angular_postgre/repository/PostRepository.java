package com.itforshort.blog_spring_angular_postgre.repository;

import com.itforshort.blog_spring_angular_postgre.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

//import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Override
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByPublished(boolean published, Pageable pageable);
    Page<Post> findByTitleContaining(String title, Pageable pageable);
}
