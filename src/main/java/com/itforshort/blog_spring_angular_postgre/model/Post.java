package com.itforshort.blog_spring_angular_postgre.model;

import javax.persistence.*;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "detail")
    private String detail;

    @Column(name = "published")
    private boolean published;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Post(String title, String description, String detail, boolean published) {
        this.title = title;
        this.description = description;
        this.detail = detail;
        this.published = published;
    }

    public Post() {}

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Override
    public String toString() {
        return "Post [id=" + id + ", title=" + title + ", desc=" + description + ", published=" + published + "]";
    }
}
