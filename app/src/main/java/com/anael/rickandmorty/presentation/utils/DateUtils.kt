package com.anael.rickandmorty.presentation.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

/**
 * Small util for format date (Locale)
 */
object DateUtils {
    /**
     * Tries multiple common patterns from the API/sample:
     *  - "Thursday 1st November, 2019"
     *  - "December 2, 2013"
     *  - "1 November 2019"
     *  - "01-11-2019" or "2019-11-01" (fallbacks)
     * If parsing fails, returns the original string.
     */
    fun formatDateDdMmYyyy(input: String): String {
        val outFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

        // Helper that strips English ordinal suffixes: 1st, 2nd, 3rd, 4th...
        fun stripOrdinals(s: String): String =
            s.replace(Regex("\\b(\\d{1,2})(st|nd|rd|th)\\b"), "$1")

        val cleaned = stripOrdinals(input).trim()

        val candidates: List<DateTimeFormatter> = listOf(
            // "Thursday 1 November, 2019"
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("EEEE d MMMM, uuuu")
                .toFormatter(Locale.ENGLISH),

            // "December 2, 2013"
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d, uuuu")
                .toFormatter(Locale.ENGLISH),

            // "1 November 2019"
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("d MMMM uuuu")
                .toFormatter(Locale.ENGLISH),

            // "01-11-2019"
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MM-uuuu")
                .toFormatter(Locale.ENGLISH),

            // "2019-11-01"
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("uuuu-MM-dd")
                .toFormatter(Locale.ENGLISH),

            // Very forgiving: "d M uuuu" (e.g., 1 11 2019)
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral(' ')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral(' ')
                .appendValue(ChronoField.YEAR)
                .toFormatter(Locale.ENGLISH)
        )

        val parsed: LocalDate? = candidates.firstNotNullOfOrNull { fmt ->
            try { LocalDate.parse(cleaned, fmt) } catch (_: DateTimeParseException) { null }
        }

        return parsed?.format(outFormatter) ?: input
    }
}
