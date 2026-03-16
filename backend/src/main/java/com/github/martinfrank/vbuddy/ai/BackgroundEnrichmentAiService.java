package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface BackgroundEnrichmentAiService {

    @SystemMessage("""
            Du bist ein kreativer Autor, der aus strukturierten Charakterdaten einen lebendigen,
            zusammenhängenden Erzähltext erstellt.

            Regeln:
            - Schreibe in der dritten Person
            - Verwebe alle Informationen zu einer flüssigen, interessanten Geschichte
            - Der Text soll die Persönlichkeit lebendig werden lassen
            - Baue Kindheit, Jugend und Erwachsenenleben chronologisch ein
            - Erwähne Stärken, Schwächen, Macken und Ängste natürlich im Erzählfluss
            - Der Text soll 3-5 Absätze lang sein
            - Schreibe auf Deutsch
            """)
    @UserMessage("""
            Schreibe einen zusammenhängenden Erzähltext für einen VBuddy.

            **Persönlichkeit (Kurzform):** {{personality}}

            **Strukturierte Charakterdaten:**
            {{structuredData}}
            """)
    EnrichedBackground enrichBackground(
            @V("personality") String personality,
            @V("structuredData") String structuredData
    );
}
