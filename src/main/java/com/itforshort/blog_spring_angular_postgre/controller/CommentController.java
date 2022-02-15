package com.itforshort.blog_spring_angular_postgre.controller;

import com.itforshort.blog_spring_angular_postgre.model.Comment;
import com.itforshort.blog_spring_angular_postgre.model.Post;
import com.itforshort.blog_spring_angular_postgre.repository.CommentRepository;
import com.itforshort.blog_spring_angular_postgre.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentController(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/posts/{post_id}/comments")
    public ResponseEntity<List<Comment>> getAllCommentsByPostId(@PathVariable("post_id") Long post_id) {
        if (!postRepository.existsById(post_id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<Comment> comments = commentRepository.findByPostId(post_id);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        }
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") long id) {
        Optional<Comment> commentData = commentRepository.findById(id);
        return commentData.map(comment -> new ResponseEntity<>(comment, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable("postId") Long postId, @RequestBody Comment commentRequest) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent()) {
            Post _post = post.get();
            commentRequest.setPost(_post);
            Comment comment = commentRepository.save(commentRequest);
            return new ResponseEntity<>(comment, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable("id") long id, @RequestBody Comment comment) {
        Optional<Comment> commentDAta = commentRepository.findById(id);
        if (commentDAta.isPresent()) {
            Comment _comment = commentDAta.get();
            _comment.setText(comment.getText());
            return new ResponseEntity<>(commentRepository.save(_comment), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<HttpStatus> deletePost(@PathVariable("id") long id) {
        try {
            commentRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/posts/{postId}/comments")
    public ResponseEntity<HttpStatus> deleteCommentByPostId(@PathVariable("postId") Long postId) {
        try {
            commentRepository.deleteByPostId(postId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
