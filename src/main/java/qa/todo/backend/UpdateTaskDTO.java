package qa.todo.backend;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateTaskDTO {
    @Size(min = 4, message = "Название должно содержать минимум 4 символа")
    private String name;

    private String description;
    private LocalDate deadline;
    private Priorities priority;
}