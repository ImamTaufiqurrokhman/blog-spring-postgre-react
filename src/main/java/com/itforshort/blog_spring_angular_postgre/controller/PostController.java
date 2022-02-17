package com.itforshort.blog_spring_angular_postgre.controller;

import com.itforshort.blog_spring_angular_postgre.model.Post;
import com.itforshort.blog_spring_angular_postgre.repository.PostRepository;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//import static javax.swing.text.html.parser.DTDConstants.ID;

//@CrossOrigin(origins = "http://localhost:8081")
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class PostController {
    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "3") int size,
            @RequestParam(required = false, defaultValue = "id") String sort_by,
            @RequestParam(required = false, defaultValue = "desc") String sort_type
    ) {
        try {
//            List<Order> orders = new ArrayList<Order>();
//            if (sort[0].contains(",")) {
//                for (String sortOrder : sort) {
//                    String[] _sort = sortOrder.split(",");
//                    orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
//                }
//            } else {
//                orders.add(new Order(getSortDirection(sort[1]), sort[0]));
//            }
            Pageable paging = PageRequest.of(page - 1, size, getSortDirection(sort_by, sort_type));
            Page<Post> pagePosts;
            if (title == null) {
                pagePosts = postRepository.findAll(paging);
            } else
                pagePosts = postRepository.findByTitleContaining(title, paging);
            return getMapResponseEntity(pagePosts);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable("id") long id) {
        Optional<Post> postData = postRepository.findById(id);
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
    public ResponseEntity<Map<String, Object>> getByPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        try {
            Pageable paging = PageRequest.of(page - 1, size);
            Page<Post> pagePosts;
            pagePosts = postRepository.findByPublished(true, paging);
            return getMapResponseEntity(pagePosts);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> getMapResponseEntity(Page<Post> pagePosts) {
        List<Post> posts;
        posts = pagePosts.getContent();
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("currentPage", pagePosts.getNumber() + 1);
        response.put("totalItems", pagePosts.getTotalElements());
        response.put("totalPages", pagePosts.getTotalPages());
        if (posts.isEmpty())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Sort getSortDirection(String sort_by, String sort_type) {
        if (sort_type.equals("asc")) {
            return Sort.by(sort_by).ascending();
        } else if (sort_type.equals("desc")) {
            return Sort.by(sort_by).descending();
        }
        return Sort.by(sort_by).ascending();
    }

}
