package com.example.carforum.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title")
    @Size(min = 16, max = 64)
    private String title;

    @Column(name = "content")
    @Size(min = 32, max = 8192)
    private String content;

    @Column(name = "likes")
    private int likes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Post(){}

    public Post(int id, String title, String content, int likes, User user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    private void setContent(String content) {
        this.content = content;
    }

    public int getLikes() {
        return likes;
    }

    private void setLikes(int likes) {
        this.likes = likes;
    }

    public User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }
}
