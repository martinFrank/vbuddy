package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Shared configuration for LLM integration tests.
 * Builds ChatLanguageModel instances and AiServices without Spring context.
 * Requires a running Ollama instance at the configured URL.
 */
public final class LlmTestConfig {

    private static final String BASE_URL = System.getenv().getOrDefault("LLM_TEST_BASE_URL", "http://192.168.0.251:11434/v1");
    private static final String API_KEY = System.getenv().getOrDefault("LLM_TEST_API_KEY", "ollama");
    private static final String PLANNING_MODEL = System.getenv().getOrDefault("LLM_TEST_PLANNING_MODEL", "qwen3:14b");
    private static final String EXECUTION_MODEL = System.getenv().getOrDefault("LLM_TEST_EXECUTION_MODEL", "qwen3:8b");
    private static final int TIMEOUT_SECONDS = Integer.parseInt(System.getenv().getOrDefault("LLM_TEST_TIMEOUT_SECONDS", "300"));

    private LlmTestConfig() {
    }

    public static ChatLanguageModel planningModel() {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName(PLANNING_MODEL)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public static ChatLanguageModel executionModel() {
        return OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName(EXECUTION_MODEL)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public static <T> T buildService(Class<T> serviceClass, ChatLanguageModel model) {
        return AiServices.builder(serviceClass)
                .chatLanguageModel(model)
                .build();
    }

    /**
     * Flexible parser that accepts both 'yyyy-MM-dd HH:mm' and 'yyyy-MM-ddTHH:mm' formats,
     * since LLMs sometimes produce ISO format with 'T' separator despite instructions.
     */
    public static final DateTimeFormatter FLEXIBLE_PARSER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .append(DateTimeFormatter.ofPattern("HH:mm"))
            .optionalStart().appendPattern(":ss").optionalEnd()
            .toFormatter();

    public static LocalDateTime parseFlexible(String dateTime) {
        return LocalDateTime.parse(dateTime.trim(), FLEXIBLE_PARSER);
    }

    // Shared test data
    public static final String TEST_PERSONALITY = "Max ist ein 28-jähriger Softwareentwickler aus Nürnberg. " +
            "Er ist neugierig, sportlich und liebt gutes Essen. Er ist etwas introvertiert, " +
            "aber hat einen trockenen Humor.";

    public static final String TEST_NEEDS = """
            HUNGER: 65/100
            BOREDOM: 40/100
            KNOWLEDGE: 30/100
            EXERCISE: 70/100
            SOCIAL: 55/100""";

    public static final String TEST_LOCATION = "Zu Hause";

    public static final String TEST_WEEKLY_SCHEDULE = """
            Wochentag:
            00:00-07:00 Schlafen
            07:00-07:30 Morgenroutine
            07:30-08:00 Frühstück
            08:00-12:00 Arbeit
            12:00-13:00 Mittagspause
            13:00-17:00 Arbeit
            17:00-18:00 Sport
            18:00-19:00 Abendessen
            19:00-22:00 Freizeit
            22:00-00:00 Schlafen""";

    public static final String TEST_BACKGROUND_NARRATIVE = """
            Max wuchs in einem kleinen Vorort von Nürnberg auf. Schon als Kind war er fasziniert \
            von Computern und verbrachte Stunden damit, kleine Programme zu schreiben. In der Jugend \
            entdeckte er seine Leidenschaft für Sport, besonders Joggen und Klettern. Nach dem Informatikstudium \
            in Erlangen arbeitet er nun als Backend-Entwickler bei einem mittelständischen Unternehmen. \
            Abends kocht er gerne aufwendige Gerichte und liest Science-Fiction-Romane.""";
}
