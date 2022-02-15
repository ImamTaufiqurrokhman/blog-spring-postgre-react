package com.itforshort.blog_spring_angular_postgre.controller;

import com.itforshort.blog_spring_angular_postgre.model.Post;
import com.itforshort.blog_spring_angular_postgre.repository.PostRepository;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import static javax.swing.text.html.parser.DTDConstants.ID;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class PostController {
//    @Autowired
//    PostRepository postRepository;

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts(@RequestParam(required = false) String title) {
        try {
            List<Post> posts = new ArrayList<>();
            if (title == null)
                posts.addAll(postRepository.findAll());
            else
//                postRepository.findByTitleContaining(title).forEach(posts::add);
                posts.addAll(postRepository.findByTitleContaining(title));
            if (posts.isEmpty())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable("id") long id) {
        Optional<Post> postData = postRepository.findById(id);
//        if (postData.isPresent()) {
//            return new ResponseEntity<>(postData.get(), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }

        return postData.map(post -> new ResponseEntity<>(post, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        try {
            Post _post = postRepository
                    .save(new Post(post.getTitle(), post.getDescription(), post.getDetail(), false));
            return new ResponseEntity<>(_post, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable("id") long id, @RequestBody Post post) {
        Optional<Post> postData = postRepository.findById(id);
        if (postData.isPresent()) {
            Post _post = postData.get();
            _post.setTitle(post.getTitle());
            _post.setDescription(post.getDescription());
            _post.setDetail(post.getDetail());
            _post.setPublished(post.isPublished());

            return new ResponseEntity<>(postRepository.save(_post), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<HttpStatus> deletePost(@PathVariable("id") long id) {
        try {
            postRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/posts/all")
    public ResponseEntity<HttpStatus> deleteAllPosts() {
        try {
            postRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/posts/published")
    public ResponseEntity<List<Post>> getByPublished() {
        try {
            List<Post> posts = postRepository.findByPublished(true);
            if (posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(posts, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
