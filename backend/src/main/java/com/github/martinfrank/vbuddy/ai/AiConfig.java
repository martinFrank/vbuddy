package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
