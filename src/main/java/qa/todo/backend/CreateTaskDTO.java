package qa.todo.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskDTO {
    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 4, message = "минимальная длина названия - 4 символа")
    private String name;

    private String description;
    private LocalDate deadline;
    private Priorities priority = Priorities.MEDIUM;
}
