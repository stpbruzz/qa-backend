package qa.todo.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskDTO {
    private String name;
    private String description;
    private LocalDate deadline;
    private Priorities priority;
}
