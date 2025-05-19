package qa.todo.backend;

import java.time.LocalDate;
import java.time.Year;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MacroProcessor {
    public static Priorities parsePriority(String name) {
        if (Pattern.compile("!1(\\D|$)").matcher(name).find()) return Priorities.CRITICAL;
        if (Pattern.compile("!2(\\D|$)").matcher(name).find()) return Priorities.HIGH;
        if (Pattern.compile("!3(\\D|$)").matcher(name).find()) return Priorities.MEDIUM;
        if (Pattern.compile("!4(\\D|$)").matcher(name).find()) return Priorities.LOW;
        return Priorities.MEDIUM;
    }

    public static LocalDate parseDeadline(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("!before\\s(\\d{2})[.-](\\d{2})[.-](\\d{4})");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            try {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));

                if (year < 1970 || year > 2100) {
                    return null;
                }

                if (month < 1 || month > 12) return null;
                if (day < 1 || day > 31) return null;

                if (month == 4 || month == 6 || month == 9 || month == 11) {
                    if (day > 30) return null;
                } else if (month == 2) {
                    boolean isLeap = Year.isLeap(year);
                    if (isLeap && day > 29) return null;
                    if (!isLeap && day > 28) return null;
                }

                return LocalDate.of(year, month, day);

            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static String clearName(String name) {
        name = name.replaceAll("!([1234])(?=\\D|$)", "");
        name = name.replaceAll("!before\\s(\\d{2})[.-](\\d{2})[.-](\\d{4})", "");

        return name.trim().replaceAll("\\s{2,}", " ");
    }
}