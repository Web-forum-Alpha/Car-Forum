package com.example.carforum.models;

import java.util.Optional;

public class FilterOptions {

    private final Optional<String> title;
    private final Optional<String> username;
    private final Optional<Integer> likesCount;
    private final Optional<Integer> commentsCount;
    private final Optional<String> sortBy;
    private final Optional<String> orderBy;

    public FilterOptions(){

        this(null, null, null, null, null, null);
    }

    public FilterOptions(String title, String username, Integer likes, Integer comments,
                         String sortBy, String orderBy){

        this.title = Optional.ofNullable(title).filter(v -> !v.isBlank());
        this.username = Optional.ofNullable(username).filter(v -> !v.isBlank());
        this.likesCount = Optional.ofNullable(likes);
        this.commentsCount = Optional.ofNullable(comments);
        this.sortBy = Optional.ofNullable(sortBy).filter(v -> !v.isBlank());
        this.orderBy = Optional.ofNullable(orderBy).filter(v -> !v.isBlank());

    }

    public Optional<String> getTitle() {
        return title;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<Integer> getLikesCount() {
        return likesCount;
    }

    public Optional<Integer> getCommentsCount() {
        return commentsCount;
    }

    public Optional<String> getSortBy() {
        return sortBy;
    }

    public Optional<String> getOrderBy() {
        return orderBy;
    }
}
