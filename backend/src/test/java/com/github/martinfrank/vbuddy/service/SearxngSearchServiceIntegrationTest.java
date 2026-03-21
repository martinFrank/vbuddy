package com.github.martinfrank.vbuddy.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class SearxngSearchServiceIntegrationTest {

    private static final String BASE_URL = System.getenv().getOrDefault("SEARXNG_TEST_BASE_URL", "http://localhost:8888");
    private static final int MAX_RESULTS = 5;
    private static final String LANGUAGE = "de";

    private static SearxngSearchService searchService;

    @BeforeAll
    static void setUp() {
        searchService = new SearxngSearchService(
                new RestTemplate(),
                BASE_URL,
                MAX_RESULTS,
                LANGUAGE,
                true
        );
    }

    @Test
    void searchLocalActivities_returnsResults() {
        List<SearchResult> results = searchService.searchLocalActivities("Nürnberg", LocalDate.now());

        assertThat(results).isNotNull().isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(MAX_RESULTS);

        for (SearchResult result : results) {
            assertThat(result.title()).as("Title").isNotBlank();
            assertThat(result.url()).as("URL").isNotBlank().startsWith("http");
        }
    }

    @Test
    void searchLocalActivities_resultsHaveSnippets() {
        List<SearchResult> results = searchService.searchLocalActivities("München", LocalDate.now());

        results.forEach(System.out::println);

        long withSnippet = results.stream()
                .filter(r -> r.snippet() != null && !r.snippet().isBlank())
                .count();
        assertThat(withSnippet)
                .as("Most results should have snippets")
                .isGreaterThan(0);
    }

    @Test
    void searchLocalBusinesses_returnsResults() {
        List<SearchResult> results = searchService.searchLocalBusinesses("Nürnberg");

        results.forEach(System.out::println);

        assertThat(results).isNotNull().isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(MAX_RESULTS);

        for (SearchResult result : results) {
            assertThat(result.title()).as("Title").isNotBlank();
            assertThat(result.url()).as("URL").isNotBlank().startsWith("http");
        }
    }

    @Test
    void searchLocalBusinesses_differentLocationsReturnDifferentResults() {
        List<SearchResult> nuernberg = searchService.searchLocalBusinesses("Nürnberg");
        List<SearchResult> berlin = searchService.searchLocalBusinesses("Berlin");

        assertThat(nuernberg).isNotEmpty();
        assertThat(berlin).isNotEmpty();

        // At least some results should differ between cities
        List<String> nuernbergUrls = nuernberg.stream().map(SearchResult::url).toList();
        List<String> berlinUrls = berlin.stream().map(SearchResult::url).toList();
        assertThat(nuernbergUrls)
                .as("Different cities should yield different URLs")
                .isNotEqualTo(berlinUrls);
    }

    @Test
    void searchImages_returnsImageUrls() {
        List<String> imageUrls = searchService.searchImages("Nürnberg Stadtpark", 3);

        assertThat(imageUrls).isNotNull().isNotEmpty();
        assertThat(imageUrls.size()).isLessThanOrEqualTo(3);

        for (String url : imageUrls) {
            assertThat(url).as("Image URL").isNotBlank().startsWith("http");
        }
    }

    @Test
    void searchImages_respectsMaxLimit() {
        List<String> oneImage = searchService.searchImages("Nürnberg Altstadt", 1);
        assertThat(oneImage.size()).isLessThanOrEqualTo(1);

        List<String> fiveImages = searchService.searchImages("Nürnberg Altstadt", 5);
        assertThat(fiveImages.size()).isLessThanOrEqualTo(5);
    }

    @Test
    void formatResultsAsText_producesReadableOutput() {
        List<SearchResult> results = searchService.searchLocalActivities("Nürnberg", LocalDate.now());

        String formatted = searchService.formatResultsAsText(results);

        assertThat(formatted).isNotBlank();
        assertThat(formatted).contains("1.");
        assertThat(formatted).contains("Quelle:");
        // Each result should be numbered
        for (int i = 1; i <= results.size(); i++) {
            assertThat(formatted).contains(i + ".");
        }
    }

    @Test
    void disabledService_returnsEmptyResults() {
        SearxngSearchService disabled = new SearxngSearchService(
                new RestTemplate(),
                BASE_URL,
                MAX_RESULTS,
                LANGUAGE,
                false
        );

        assertThat(disabled.searchLocalActivities("Nürnberg", LocalDate.now())).isEmpty();
        assertThat(disabled.searchLocalBusinesses("Nürnberg")).isEmpty();
        assertThat(disabled.searchImages("test", 3)).isEmpty();
    }
}
