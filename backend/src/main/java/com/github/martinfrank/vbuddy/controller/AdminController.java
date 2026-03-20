package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.service.EmbeddingBackfillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmbeddingBackfillService embeddingBackfillService;

    public AdminController(EmbeddingBackfillService embeddingBackfillService) {
        this.embeddingBackfillService = embeddingBackfillService;
    }

    @PostMapping("/backfill-embeddings")
    public ResponseEntity<String> backfillEmbeddings() {
        embeddingBackfillService.backfillAll();
        return ResponseEntity.ok("Embedding backfill completed");
    }
}
