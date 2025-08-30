package com.example.thenews;

public class newsitem {
    private String title;
    private String description;
    private String source;
    private String publishedAt;
    private String url;
    private String imageUrl;

    public newsitem(String title, String description, String source, String publishedAt, String url) {
        this.title = title;
        this.description = description != null && !description.equals("null") ? description : "No description available.";
        this.source = source;
        this.publishedAt = publishedAt;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}