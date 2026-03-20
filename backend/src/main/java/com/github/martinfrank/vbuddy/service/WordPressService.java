package com.github.martinfrank.vbuddy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class WordPressService {

    private static final Logger log = LoggerFactory.getLogger(WordPressService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseApiUrl;
    private final String authHeader;
    private final boolean enabled;

    public WordPressService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${vbuddy.wordpress.url:}") String url,
            @Value("${vbuddy.wordpress.username:}") String username,
            @Value("${vbuddy.wordpress.password:}") String password) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.enabled = !url.isBlank() && !username.isBlank() && !password.isBlank();
        this.baseApiUrl = url.isBlank() ? "" : url.replaceAll("/$", "") + "/?rest_route=/wp/v2";

        if (enabled) {
            String credentials = username + ":" + password;
            this.authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        } else {
            this.authHeader = "";
            log.info("WordPress-Integration ist deaktiviert (keine Zugangsdaten konfiguriert)");
        }
    }

    public void publishPost(String title, String content, List<String> imageUrls) {
        if (!enabled) {
            log.debug("WordPress-Veröffentlichung übersprungen (deaktiviert)");
            return;
        }

        try {
            List<String> wpImageUrls = uploadImages(imageUrls);
            String contentWithImages = embedImages(content, wpImageUrls);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);

            Map<String, Object> body = Map.of(
                    "title", title,
                    "content", contentWithImages,
                    "status", "publish"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseApiUrl + "/posts", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Blogartikel '{}' auf WordPress veröffentlicht ({} Bilder)",
                        title, wpImageUrls.size());
            } else {
                log.warn("WordPress-Veröffentlichung fehlgeschlagen (Status {}): {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.warn("WordPress-Veröffentlichung fehlgeschlagen für '{}': {}", title, e.getMessage());
        }
    }

    private List<String> uploadImages(List<String> imageUrls) {
        List<String> wpUrls = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            try {
                byte[] imageData = restTemplate.getForObject(imageUrl, byte[].class);
                if (imageData == null || imageData.length == 0) {
                    continue;
                }

                String filename = extractFilename(imageUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                headers.setContentType(guessMediaType(filename));
                headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");

                HttpEntity<byte[]> request = new HttpEntity<>(imageData, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        baseApiUrl + "/media", request, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode json = objectMapper.readTree(response.getBody());
                    String wpUrl = json.path("source_url").asText("");
                    if (!wpUrl.isBlank()) {
                        wpUrls.add(wpUrl);
                        log.info("Bild hochgeladen: {}", wpUrl);
                    }
                }
            } catch (Exception e) {
                log.warn("Bild-Upload fehlgeschlagen für '{}': {}", imageUrl, e.getMessage());
            }
        }

        return wpUrls;
    }

    private String embedImages(String content, List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return content;
        }

        StringBuilder sb = new StringBuilder(content);
        sb.append("\n\n");
        for (String url : imageUrls) {
            sb.append(String.format(
                    "<!-- wp:image --><figure class=\"wp-block-image\"><img src=\"%s\" alt=\"\"/></figure><!-- /wp:image -->\n",
                    url));
        }
        return sb.toString();
    }

    private String extractFilename(String url) {
        String path = url.split("\\?")[0];
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (name.isBlank() || !name.contains(".")) {
            name = "vbuddy-image.jpg";
        }
        return name;
    }

    private MediaType guessMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}
