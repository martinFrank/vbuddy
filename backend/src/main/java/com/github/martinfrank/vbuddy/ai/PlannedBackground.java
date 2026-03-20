package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

@Description("Strukturiertes Charakterprofil als flache Struktur — alle Felder sind einfache Strings, keine verschachtelten Objekte")
public record PlannedBackground(
        @Description("Alter als Zahl")
        int alter,

        @Description("Aussehen in einem Satz, z.B. 'Mittelgroß, braune Haare, grüne Augen, sportliche Figur'")
        String aussehen,

        @Description("Herkunftsort in einem Satz, z.B. 'Geboren und aufgewachsen in Nürnberg'")
        String herkunft,

        @Description("Hobbys als Fließtext, z.B. 'Gärtnern, Wandern, Kegeln und Brettspiele mit der Familie'")
        String hobbys,

        @Description("Persönliche Macken als Fließtext, z.B. 'Trinkt Kaffee nur aus einer bestimmten Tasse'")
        String macken,

        @Description("Ängste als Fließtext, z.B. 'Höhenangst und Sorge um die Zukunft der Kinder'")
        String aengste,

        @Description("Ziele und Motivationen als Fließtext")
        String zieleUndMotivationen,

        @Description("Stärken als Fließtext")
        String staerken,

        @Description("Schwächen als Fließtext")
        String schwaechen,

        @Description("Kindheit als Fließtext, 2-3 Sätze")
        String kindheit,

        @Description("Jugend als Fließtext, 2-3 Sätze")
        String jugend,

        @Description("Erwachsenenleben als Fließtext, 2-3 Sätze")
        String erwachsenenleben,

        @Description("Begründung der getroffenen Entscheidungen")
        String reasoning
) {}
