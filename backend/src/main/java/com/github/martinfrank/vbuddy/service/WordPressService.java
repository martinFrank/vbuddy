package com.github.martinfrank.vbuddy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class WordPressService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String authHeader;
    private final boolean enabled;

    public WordPressService(
            RestTemplate restTemplate,
            @Value("${vbuddy.wordpress.url:}") String url,
            @Value("${vbuddy.wordpress.username:}") String username,
            @Value("${vbuddy.wordpress.password:}") String password) {
        this.restTemplate = restTemplate;
        this.enabled = !url.isBlank() && !username.isBlank() && !password.isBlank();
        this.apiUrl = url.isBlank() ? "" : url.replaceAll("/$", "") + "/wp-json/wp/v2/posts";

        if (enabled) {
            String credentials = username + ":" + password;
            this.authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        } else {
            this.authHeader = "";
            log.info("WordPress-Integration ist deaktiviert (keine Zugangsdaten konfiguriert)");
        }
    }

    public void publishPost(String title, String content) {
        if (!enabled) {
            log.debug("WordPress-Veröffentlichung übersprungen (deaktiviert)");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);

            Map<String, Object> body = Map.of(
                    "title", title,
                    "content", content,
                    "status", "publish"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Blogartikel '{}' auf WordPress veröffentlicht", title);
            } else {
                log.warn("WordPress-Veröffentlichung fehlgeschlagen (Status {}): {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.warn("WordPress-Veröffentlichung fehlgeschlagen für '{}': {}", title, e.getMessage());
        }
    }
}
