package com.github.martinfrank.vbuddy.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearxngResult(String title, String url, String content) {
}
