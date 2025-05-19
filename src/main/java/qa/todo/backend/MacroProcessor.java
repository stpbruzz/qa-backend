package qa.todo.backend;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MacroProcessor {
    public static Priorities parsePriority(String name) {
        if (name.contains("!1")) return Priorities.CRITICAL;
        if (name.contains("!2")) return Priorities.HIGH;
        if (name.contains("!3")) return Priorities.MEDIUM;
        if (name.contains("!4")) return Priorities.LOW;
        return Priorities.MEDIUM;
    }

    public static LocalDate parseDeadline(String name) {
        if (name.contains("!before")) {
            String[] parts = name.split("!before ");
            if (parts.length > 1) {
                String dateStr = parts[1].split(" ")[0].replace("-", ".");
                LocalDate deadline = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            }
        }
        return null;
    }

    public static String clearName(String name) {
        name = name.replaceAll("!(1|2|3|4)", "");
        name = name.replaceAll("!before\\s\\d{2}[.-]\\d{2}[.-]\\d{4}", "");

        return name.trim().replaceAll("\\s{2,}", " ");
    }
}
