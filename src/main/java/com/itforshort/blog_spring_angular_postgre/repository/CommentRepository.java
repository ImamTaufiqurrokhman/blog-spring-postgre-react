package com.itforshort.blog_spring_angular_postgre.repository;

import com.itforshort.blog_spring_angular_postgre.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    @Transactional
    void deleteByPostId(long postId);
}
