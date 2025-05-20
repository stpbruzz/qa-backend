package qa.todo.backend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskEntity createTask(createTaskDTO taskDTO) {
        TaskEntity newTask = new TaskEntity();

        newTask.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : MacroProcessor.parsePriority(taskDTO.getName()));
        newTask.setDeadline(taskDTO.getDeadline() != null ? taskDTO.getDeadline() : MacroProcessor.parseDeadline(taskDTO.getName()));
        newTask.setName(MacroProcessor.clearName(taskDTO.getName()));
        newTask.setDescription(taskDTO.getDescription());

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

    public TaskEntity updateTask(String id, editTaskDTO taskDTO) {
        TaskEntity task = findById(id);

        if (taskDTO.getName() != null) {
            task.setName(taskDTO.getName());
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(taskDTO.getPriority());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
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

