package qa.todo.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование обработки макросов приоритета")
class MacroProcessorPriorityTest {

    @DisplayName("Корректные макросы приоритета")
    @ParameterizedTest(name = "Входная строка: \"{0}\" → Ожидаемый приоритет: {1}")
    @CsvSource({
            "'!1', CRITICAL",                  // минимальный приоритет
            "'!4', LOW",                       // максимальный приоритет
            "'Важно!1', CRITICAL",             // макрос в конце
            "'!1Срочно', CRITICAL",            // макрос в начале
            "'Сделать!2', HIGH",               // средний приоритет
            "'Задача!3 тест', MEDIUM",         // с дополнительным текстом
            "'!4Неважно', LOW",                // минимальный приоритет с текстом

            "'!1 ', CRITICAL",                 // пробел после
            "' !1', CRITICAL",                 // пробел перед
            "'  !1  ', CRITICAL",              // пробелы вокруг
            "'prefix!1suffix', CRITICAL",      // текст вокруг
            "'!1 !2', CRITICAL"                // несколько приоритетов
    })
    void shouldParseValidPriorityMarkers(String input, Priorities expected) {
        assertEquals(expected, MacroProcessor.parsePriority(input));
    }

    @DisplayName("Некорректные макросы приоритета")
    @ParameterizedTest(name = "Входная строка: \"{0}\" → Ожидаемый приоритет: MEDIUM")
    @ValueSource(strings = {
            "'!0'", "'!5'", "'!9'", "'!-2'",

            "'!11'", "'!a1'", "'! 1'",

            "''", "' '", "'null'", "'!null'"
    })
    void shouldUseDefaultForInvalidPriorityMarkers(String input) {
        assertEquals(Priorities.MEDIUM, MacroProcessor.parsePriority(input));
    }
}