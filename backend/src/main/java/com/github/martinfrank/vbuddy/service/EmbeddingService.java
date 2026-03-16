package com.github.martinfrank.vbuddy.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public void embedBackground(Long buddyId, String narrativeText) {
        // Remove old background embeddings for this buddy
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("buddy_id").isEqualTo(buddyId.toString())
                .and(MetadataFilterBuilder.metadataKey("type").isEqualTo("background")));

        Metadata metadata = new Metadata();
        metadata.put("buddy_id", buddyId.toString());
        metadata.put("type", "background");

        TextSegment segment = TextSegment.from(narrativeText, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        log.info("Background narrative embedded for buddy {}", buddyId);
    }

    public void embedCompletedTask(Long buddyId, Long taskId, String title, String description, String location) {
        String text = String.format("Aktivität: %s\nBeschreibung: %s\nOrt: %s", title, description, location);

        Metadata metadata = new Metadata();
        metadata.put("buddy_id", buddyId.toString());
        metadata.put("type", "task");
        metadata.put("task_id", taskId.toString());

        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        log.info("Completed task {} embedded for buddy {}", taskId, buddyId);
    }

    public List<String> retrieveRelevantContext(Long buddyId, String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(0.5)
                .filter(MetadataFilterBuilder.metadataKey("buddy_id").isEqualTo(buddyId.toString()))
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> match.embedded().text())
                .toList();
    }
}
