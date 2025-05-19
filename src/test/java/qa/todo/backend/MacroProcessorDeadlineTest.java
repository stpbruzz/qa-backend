package qa.todo.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование обработки макроса дедлайна")
class MacroProcessorDeadlineTest {

    @DisplayName("Корректные форматы дат")
    @ParameterizedTest(name = "Входная строка: \"{0}\" → Ожидаемый результат: {1}")
    @CsvSource({
            // Основные валидные форматы
            "'!before 31.12.2024', '2024-12-31'",       // стандартный формат
            "'!before 31-12-2024', '2024-12-31'",       // альтернативный разделитель
            "'!before 01.01.1970', '1970-01-01'",       // минимальная граница года
            "'!before 31.12.2099', '2099-12-31'",       // максимальная граница года

            // Граничные значения дней
            "'!before 31.01.2023', '2023-01-31'",       // 31 день в месяце с 31 днем
            "'!before 30.04.2023', '2023-04-30'",       // 30 дней в месяце с 30 днями
            "'!before 28.02.2023', '2023-02-28'",       // февраль невисокосного года
            "'!before 29.02.2024', '2024-02-29'",       // февраль високосного года

            // Позиции макроса в строке
            "'Задача!before 15.07.2024', '2024-07-15'", // макрос в середине
            "'!before 15.07.2024 задача', '2024-07-15'", // макрос в начале
            "'!before 01.01.2025name', '2025-01-01'"     // текст сразу после даты
    })
    void shouldParseValidDeadlineFormats(String input, String expectedDate) {
        LocalDate expected = LocalDate.parse(expectedDate);
        assertEquals(expected, MacroProcessor.parseDeadline(input));
    }

    @DisplayName("Некорректные форматы дат")
    @ParameterizedTest(name = "Входная строка: \"{0}\" → Ожидаемый результат: null")
    @ValueSource(strings = {
            // Отсутствие обязательных компонентов
            "Нет макроса",
            "!before",
            "!before31.12.2024",       // отсутствие пробела

            // Неправильные форматы дат
            "!before 2025.12.31",      // обратный формат
            "!before 31/12/2025",      // неправильный разделитель
            "!before 1.1.2025",        // однозначные числа
            "!before 01.1.2025",       // смешанный формат
            "!before 1.01.2025",       // смешанный формат
            "!before 1.1.25",          // неполный год

            // Некорректные значения дат
            "!before 32.12.2025",      // несуществующий день
            "!before 31.13.2025",      // несуществующий месяц
            "!before 31.02.2025",      // несуществующая дата
            "!before 29.02.2025",      // 29 февраля не в високосный год
            "!before 01.01.2101",     // слишком большой год
            "!before 31.12.1969",      // слишком маленький год
            "!before -19.05.2025",

            // Некорректные символы
            "!before abc",             // нечисловые символы
            "!before 31.12.20ab"       // частично нечисловые
    })
    void shouldRejectInvalidDeadlineFormats(String input) {
        assertNull(MacroProcessor.parseDeadline(input));
    }
}