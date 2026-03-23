package com.github.martinfrank.vbuddy.service;

public record SearchResult(String title, String url, String snippet, String pageContent) {

    public SearchResult(String title, String url, String snippet) {
        this(title, url, snippet, null);
    }

    public SearchResult withPageContent(String pageContent) {
        return new SearchResult(title, url, snippet, pageContent);
    }
}
