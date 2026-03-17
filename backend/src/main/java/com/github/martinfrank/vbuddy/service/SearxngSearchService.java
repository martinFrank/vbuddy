package com.github.martinfrank.vbuddy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class SearxngSearchService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMAN);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int maxResults;
    private final String language;
    private final boolean enabled;

    public SearxngSearchService(
            RestTemplate restTemplate,
            @Value("${vbuddy.searxng.base-url:http://localhost:8888}") String baseUrl,
            @Value("${vbuddy.searxng.max-results:5}") int maxResults,
            @Value("${vbuddy.searxng.language:de}") String language,
            @Value("${vbuddy.searxng.enabled:true}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.maxResults = maxResults;
        this.language = language;
        this.enabled = enabled;
    }

    public List<SearchResult> searchLocalActivities(String location, LocalDate date) {
        if (!enabled) {
            log.debug("SearXNG-Suche ist deaktiviert");
            return Collections.emptyList();
        }

        String dateText = date.format(DATE_FORMATTER);
        String query = String.format("Veranstaltungen Aktivitäten %s %s", location, dateText);

        return executeSearch(query);
    }

    public List<SearchResult> searchLocalBusinesses(String location) {
        if (!enabled) {
            log.debug("SearXNG-Suche ist deaktiviert");
            return Collections.emptyList();
        }

        String query = String.format("Geschäfte Restaurants Cafés Läden %s", location);

        return executeSearch(query);
    }

    private List<SearchResult> executeSearch(String query) {
        log.info("SearXNG-Suche: '{}'", query);

        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("language", language)
                    .build()
                    .toUriString();

            SearxngResponse response = restTemplate.getForObject(url, SearxngResponse.class);

            if (response == null || response.results() == null) {
                log.warn("SearXNG hat keine Ergebnisse zurückgeliefert für: {}", query);
                return Collections.emptyList();
            }

            List<SearchResult> results = response.results().stream()
                    .limit(maxResults)
                    .map(r -> new SearchResult(r.title(), r.url(), r.content()))
                    .toList();

            log.info("SearXNG-Suche ergab {} Ergebnisse für: {}", results.size(), query);
            return results;

        } catch (Exception e) {
            log.warn("SearXNG-Suche fehlgeschlagen für '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    public String formatResultsAsText(List<SearchResult> results) {
        if (results.isEmpty()) {
            return "Keine Ergebnisse gefunden.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            SearchResult r = results.get(i);
            sb.append(String.format("%d. %s\n   %s\n   Quelle: %s\n",
                    i + 1, r.title(), r.snippet() != null ? r.snippet() : "", r.url()));
        }
        return sb.toString().trim();
    }
}
