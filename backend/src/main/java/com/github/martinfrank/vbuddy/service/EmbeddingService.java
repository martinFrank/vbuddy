package com.github.martinfrank.vbuddy.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public EmbeddingService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

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

    public void embedTask(VBuddyTask task) {
        Long buddyId = task.getBuddy().getId();
        Long taskId = task.getId();

        // Remove old embedding for this task before re-embedding
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("task_id").isEqualTo(taskId.toString()));

        String statusLabel = switch (task.getStatus()) {
            case PLANNED -> "Geplant";
            case IN_PROGRESS -> "Aktiv";
            case COMPLETED -> "Erledigt";
            case SKIPPED -> "Übersprungen";
            case ABORTED -> "Abgebrochen";
        };

        String text = String.format("Aktivität: %s\nStatus: %s\nZeitpunkt: %s\nDauer: %d Minuten\nOrt: %s\nBeschreibung: %s",
                task.getTitle(), statusLabel,
                task.getStartTime().format(FORMATTER), task.getDurationMinutes(),
                task.getLocation(), task.getDescription());

        Metadata metadata = new Metadata();
        metadata.put("buddy_id", buddyId.toString());
        metadata.put("type", "task");
        metadata.put("task_id", taskId.toString());
        metadata.put("status", task.getStatus().name());

        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);

        log.info("{} task {} embedded for buddy {}", statusLabel, taskId, buddyId);
    }

    public void removeAllByBuddyId(Long buddyId) {
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("buddy_id").isEqualTo(buddyId.toString()));
        log.info("All embeddings removed for buddy {}", buddyId);
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
