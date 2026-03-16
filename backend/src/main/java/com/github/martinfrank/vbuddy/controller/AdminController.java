package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.service.EmbeddingBackfillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmbeddingBackfillService embeddingBackfillService;

    @PostMapping("/backfill-embeddings")
    public ResponseEntity<String> backfillEmbeddings() {
        embeddingBackfillService.backfillAll();
        return ResponseEntity.ok("Embedding backfill completed");
    }
}
