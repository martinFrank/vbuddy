package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel planningChatModel(
            @Value("${vbuddy.ai.planning.base-url}") String baseUrl,
            @Value("${vbuddy.ai.planning.api-key}") String apiKey,
            @Value("${vbuddy.ai.planning.model-name}") String modelName,
            @Value("${vbuddy.ai.planning.temperature}") double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public ChatLanguageModel executionChatModel(
            @Value("${vbuddy.ai.execution.base-url}") String baseUrl,
            @Value("${vbuddy.ai.execution.api-key}") String apiKey,
            @Value("${vbuddy.ai.execution.model-name}") String modelName,
            @Value("${vbuddy.ai.execution.temperature}") double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public ChatLanguageModel chatChatModel(
            @Value("${vbuddy.ai.chat.base-url}") String baseUrl,
            @Value("${vbuddy.ai.chat.api-key}") String apiKey,
            @Value("${vbuddy.ai.chat.model-name}") String modelName,
            @Value("${vbuddy.ai.chat.temperature}") double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public PlanningAiService planningAiService(ChatLanguageModel planningChatModel) {
        return AiServices.builder(PlanningAiService.class)
                .chatLanguageModel(planningChatModel)
                .build();
    }

    @Bean
    public ExecutionAiService executionAiService(ChatLanguageModel executionChatModel) {
        return AiServices.builder(ExecutionAiService.class)
                .chatLanguageModel(executionChatModel)
                .build();
    }

    @Bean
    public EnrichmentAiService enrichmentAiService(ChatLanguageModel executionChatModel) {
        return AiServices.builder(EnrichmentAiService.class)
                .chatLanguageModel(executionChatModel)
                .build();
    }

    @Bean
    public BackgroundPlanningAiService backgroundPlanningAiService(ChatLanguageModel planningChatModel) {
        return AiServices.builder(BackgroundPlanningAiService.class)
                .chatLanguageModel(planningChatModel)
                .build();
    }

    @Bean
    public BackgroundEnrichmentAiService backgroundEnrichmentAiService(ChatLanguageModel executionChatModel) {
        return AiServices.builder(BackgroundEnrichmentAiService.class)
                .chatLanguageModel(executionChatModel)
                .build();
    }

    @Bean
    public ChatLanguageModel scheduleChatModel(
            @Value("${vbuddy.ai.schedule.base-url}") String baseUrl,
            @Value("${vbuddy.ai.schedule.api-key}") String apiKey,
            @Value("${vbuddy.ai.schedule.model-name}") String modelName,
            @Value("${vbuddy.ai.schedule.temperature}") double temperature) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public SchedulePlanningAiService schedulePlanningAiService(ChatLanguageModel planningChatModel) {
        return AiServices.builder(SchedulePlanningAiService.class)
                .chatLanguageModel(planningChatModel)
                .build();
    }

    @Bean
    public ScheduleEnrichmentAiService scheduleEnrichmentAiService(ChatLanguageModel scheduleChatModel) {
        return AiServices.builder(ScheduleEnrichmentAiService.class)
                .chatLanguageModel(scheduleChatModel)
                .build();
    }

    @Bean
    public ChatPlanAnalysisAiService chatPlanAnalysisAiService(ChatLanguageModel planningChatModel) {
        return AiServices.builder(ChatPlanAnalysisAiService.class)
                .chatLanguageModel(planningChatModel)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${vbuddy.ai.embedding.base-url}") String baseUrl,
            @Value("${vbuddy.ai.embedding.api-key}") String apiKey,
            @Value("${vbuddy.ai.embedding.model-name}") String modelName) {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("vbuddy_embeddings")
                .dimension(768)
                .createTable(true)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
