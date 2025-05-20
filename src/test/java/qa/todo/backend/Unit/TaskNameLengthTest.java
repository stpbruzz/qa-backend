package qa.todo.backend.Unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование обработки длины названия")
class TaskNameLengthTest {

    private static final String StringOf127Symbols = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String StringOf128Symbols = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String StringOf200Symbols = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    boolean checkNameLength(String name) {
        return name != null && name.trim().length() >= 4 && name.trim().length() < 128;
    }

    @DisplayName("Проверка валидных длин названий")
    @ParameterizedTest(name = "Для названия \"{0}\" ожидается true")
    @ValueSource(strings = {
            "Test",             // минимальная длина (4 символа)
            "Название",         // кириллические символы
            "1234",             // цифры
            StringOf127Symbols, // максимальное значение (127 символов)
            "Name with spaces", // с пробелами
            "S!@#$"             // специальные символы
    })
    void shouldAcceptValidNameLengths(String name) {
        assertTrue(checkNameLength(name));
    }

    @DisplayName("Проверка невалидных длин названий")
    @ParameterizedTest(name = "Для названия \"{0}\" ожидается false")
    @ValueSource(strings = {
            "",                 // пустая строка
            "   ",              // пробелы
            "Tes",              // 3 символа (ниже минимума)
            StringOf128Symbols, // граничное значение (128 символов)
            StringOf200Symbols, // явно превышающее
    })
    void shouldRejectInvalidNameLengths(String name) {
        assertFalse(checkNameLength(name));
    }
}