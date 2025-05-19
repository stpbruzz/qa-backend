package qa.todo.backend;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<TaskEntity> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskDTO));
    }

    @GetMapping("/show/all")
    public ResponseEntity<List<TaskEntity>> showTasks(
            @RequestParam(required = false) Statuses status,
            @RequestParam(required = false) Priorities priority,
            @RequestParam(required = false, defaultValue = "newest") String sortOrder)
    {
        return ResponseEntity.ok(taskService.showTasks(status, priority, sortOrder));
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<TaskEntity> showTask(@PathVariable String id) {
        return ResponseEntity.ok().body(taskService.findById(id));
    }

    @DeleteMapping("/delete/all")
    public ResponseEntity<String> deleteTasks() {
        taskService.deleteAll();
        return ResponseEntity.ok().body("список задач очищен");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable String id){
        taskService.deleteById(id);
        return ResponseEntity.ok().body(String.format("задача с id: %s удалена", id));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<TaskEntity> editTask(
            @PathVariable String id,
            @Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }

    @PatchMapping("/mark/{id}")
    public ResponseEntity<TaskEntity> markTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.markTask(id));
    }
}
