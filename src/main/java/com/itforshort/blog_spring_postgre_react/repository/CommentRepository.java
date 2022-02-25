package com.itforshort.blog_spring_postgre_react.repository;

import com.itforshort.blog_spring_postgre_react.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    @Transactional
    void deleteByPostId(long postId);
}
