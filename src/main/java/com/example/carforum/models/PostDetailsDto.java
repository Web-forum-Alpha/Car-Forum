package com.example.carforum.models;

import java.util.List;

public class PostDetailsDto {

    private Post post;
    private List<Comment> comments;
    private int likes;

    public PostDetailsDto() {
    }


    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
