package com.github.martinfrank.vbuddy.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearxngResponse(List<SearxngResult> results) {
}
