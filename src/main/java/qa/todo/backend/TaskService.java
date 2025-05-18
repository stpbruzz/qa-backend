package qa.todo.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskEntity createTask(CreateTaskDTO taskDTO) {
        TaskEntity newTask = new TaskEntity();
        newTask.setName(taskDTO.getName());
        newTask.setDescription(taskDTO.getDescription());
        newTask.setDeadline(taskDTO.getDeadline());
        newTask.setPriority(taskDTO.getPriority());

        return taskRepository.save(newTask);
    }

    public List<TaskEntity> showTasks(Statuses status, Priorities priority, String sortOrder) {
        if (status == null && priority == null) {
            if ("oldest".equals(sortOrder)) {
                return taskRepository.findAllByOrderByCreationDateAsc();
            } else {
                return taskRepository.findAllByOrderByCreationDateDesc();
            }
        }
        return taskRepository.findFilteredTasks(status, priority, sortOrder);
    }

    public void deleteAll() {
        taskRepository.deleteAll();
    }

    public void deleteById(String id) {
        taskRepository.deleteById(id);
    }

    public TaskEntity findById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new Exceptions.TaskNotFoundException(String.format("Задача с id: %s не найдена", id)));
    }

    public TaskEntity updateTask(String id, UpdateTaskDTO updateTaskDTO) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new Exceptions.TaskNotFoundException(String.format("Задача с id: %s не найдена", id)));

        if (updateTaskDTO.getName() != null) {
            task.setName(updateTaskDTO.getName());
        }

        if (updateTaskDTO.getDescription() != null) {
            task.setDescription(updateTaskDTO.getDescription());
        }

        if (updateTaskDTO.getDeadline() != null) {
            task.setDeadline(updateTaskDTO.getDeadline());
        }

        if (updateTaskDTO.getPriority() != null) {
            task.setPriority(updateTaskDTO.getPriority());
        }

        task.onUpdate();

        return taskRepository.save(task);
    }

    public TaskEntity markTask(String id) {
        TaskEntity task = findById(id);
        if (task.getStatus().equals(Statuses.COMPLETED) || task.getStatus().equals(Statuses.LATE)) {
            task.markAsIncomplete();
        } else {
            task.markAsCompleted();
        }
        task.onUpdate();
        return taskRepository.save(task);
    }
}

