package qa.todo.backend;

import lombok.Data;

import java.time.LocalDate;

@Data
public class editTaskDTO {
    private String name;
    private String description;
    private LocalDate deadline;
    private Priorities priority;
}
