package qa.todo.backend.API;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qa.todo.backend.Priorities;
import qa.todo.backend.Statuses;
import qa.todo.backend.TaskEntity;
import qa.todo.backend.TaskRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("taskValidParametersProvider")
    void testCreateTaskSuccessfully(
            String name,
            @Nullable String description,
            @Nullable String priority,
            @Nullable String deadline,
            String expectedPriority,
            String expectedDeadline,
            String expectedStatus
    ) throws Exception {
        ObjectNode taskJson = JsonNodeFactory.instance.objectNode();
        taskJson.put("name", name);
        if (description != null) taskJson.put("description", description);
        if (priority != null) taskJson.put("priority", priority);
        if (deadline != null) taskJson.put("deadline", deadline);

        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value(expectedPriority))
                .andExpect(jsonPath("$.deadline").value(expectedDeadline))
                .andExpect(jsonPath("$.status").value(expectedStatus));
    }

    private static Stream<Arguments> taskValidParametersProvider() {
        return Stream.of(
                Arguments.of("Задача", "Обычная задача", "HIGH", "2025-12-31", "HIGH", "2025-12-31", "ACTIVE"),
                Arguments.of("Тест", "Минимальная длина имени", "LOW", "2025-05-31", "LOW", "2025-05-31", "ACTIVE"),
                Arguments.of("Только имя", null, null, null, "MEDIUM", null, "ACTIVE"),
                Arguments.of("Task!1", "Макрос приоритета", null, null, "CRITICAL", null, "ACTIVE"),
                Arguments.of("Task!before 01-05-2026", "Макрос даты", null, null, "MEDIUM", "2026-05-01", "ACTIVE"),
                Arguments.of("Task!1!before 01-05-2026", "Оба макроса", null, null, "CRITICAL", "2026-05-01", "ACTIVE"),
                Arguments.of("Task!1!before 01-05-2026", "Оба макроса и поля", "MEDIUM", "2025-05-26", "MEDIUM", "2025-05-26", "ACTIVE"),
                Arguments.of("Просроченная", "Должна быть OVERDUE", null, "2020-01-01", "MEDIUM", "2020-01-01", "OVERDUE")
        );
    }

    @ParameterizedTest
    @MethodSource("taskInvalidParametersProvider")
    void testCreateTaskUnsuccessfully(
            String name,
            @Nullable String description,
            @Nullable String priority,
            @Nullable String deadline
    ) throws Exception {
        ObjectNode taskJson = JsonNodeFactory.instance.objectNode();
        taskJson.put("name", name);
        if (description != null) taskJson.put("description", description);
        if (priority != null) taskJson.put("priority", priority);
        if (deadline != null) taskJson.put("deadline", deadline);

        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson.toString()))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> taskInvalidParametersProvider() {
        return Stream.of(
                Arguments.of("три", "Длина меньше 4-х символов", null, null),
                Arguments.of("    ", "Пробельчики", null, null),
                Arguments.of("Задание", "Неверные приоритет", "ULTRA HIGH", null),
                Arguments.of("Задание", "Другой формат даты", null, "2025.12.31"),
                Arguments.of("Задание", "Другой разделитель даты", null, "31/12/2025"),
                Arguments.of("Задание", "Не дата", null, "каво?"),
                Arguments.of("Задание", "Невисокосный год", null, "29.02.2025"),
                Arguments.of("Задание", "Несуществующий день", null, "32.12.2025")
        );
    }

    @ParameterizedTest
    @MethodSource("showTasksFiltersProvider")
    void testShowTasksWithFilters(
            Statuses status,
            Priorities priority,
            List<String> expectedTaskNames
    ) throws Exception {
        List<TaskEntity> testTasks = Arrays.asList(
                createTestTask("Task 1", Priorities.HIGH, Statuses.ACTIVE),
                createTestTask("Task 2", Priorities.MEDIUM, Statuses.ACTIVE),
                createTestTask("Task 3", Priorities.LOW, Statuses.COMPLETED),
                createTestTask("Task 4", Priorities.HIGH, Statuses.ACTIVE)
        );
        taskRepository.saveAll(testTasks);

        mockMvc.perform(MockMvcRequestBuilders.get("/show/all")
                        .param("status", status != null ? status.name() : "")
                        .param("priority", priority != null ? priority.name() : ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedTaskNames.size())))
                .andExpect(jsonPath("$[*].name").value(containsInAnyOrder(expectedTaskNames.toArray())));
    }

    private TaskEntity createTestTask(String name, Priorities priority, Statuses status) {
        TaskEntity task = new TaskEntity();
        task.setName(name);
        task.setPriority(priority);
        task.setStatus(status);
        return task;
    }

    private static Stream<Arguments> showTasksFiltersProvider() {
        return Stream.of(
                // Без фильтров - все задачи
                Arguments.of(null, null, List.of("Task 1", "Task 2", "Task 3", "Task 4")),
                // Только по статусу ACTIVE
                Arguments.of(Statuses.ACTIVE, null, List.of("Task 1", "Task 2", "Task 4")),
                // Только по HIGH приоритету
                Arguments.of(null, Priorities.HIGH, List.of("Task 1", "Task 4")),
                // Комбинированный фильтр ACTIVE + HIGH
                Arguments.of(Statuses.ACTIVE, Priorities.HIGH, List.of("Task 1", "Task 4")),
                // Только COMPLETED задачи
                Arguments.of(Statuses.COMPLETED, null, List.of("Task 3")),
                // Несуществующая комбинация
                Arguments.of(Statuses.COMPLETED, Priorities.HIGH, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("editTaskParametersProvider")
    void testEditTask(
            String originalName, Priorities originalPriority, Statuses originalStatus,
            String newName, String newPriority, String newDescription,
            String expectedName, Priorities expectedPriority
    ) throws Exception {
        TaskEntity originalTask = new TaskEntity();
        originalTask.setName(originalName);
        originalTask.setPriority(originalPriority);
        originalTask.setStatus(originalStatus);
        TaskEntity savedTask = taskRepository.save(originalTask);

        ObjectNode editJson = JsonNodeFactory.instance.objectNode();
        editJson.put("name", newName);
        if (newPriority != null) editJson.put("priority", newPriority);
        if (newDescription != null) editJson.put("description", newDescription);

        mockMvc.perform(MockMvcRequestBuilders.put("/edit/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editJson.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.name").value(expectedName))
                .andExpect(jsonPath("$.priority").value(expectedPriority.name()))
                .andExpect(jsonPath("$.status").value(originalStatus.name()));
    }

    private static Stream<Arguments> editTaskParametersProvider() {
        return Stream.of(
                // Полное обновление
                Arguments.of("Old Name", Priorities.LOW, Statuses.ACTIVE,
                        "New Name", "HIGH", "New Description",
                        "New Name", Priorities.HIGH),
                // Только имя
                Arguments.of("Old Name", Priorities.MEDIUM, Statuses.ACTIVE,
                        "Updated Name", null, null,
                        "Updated Name", Priorities.MEDIUM),
                // Только приоритет
                Arguments.of("Original", Priorities.HIGH, Statuses.COMPLETED,
                        null, "LOW", null,
                        "Original", Priorities.LOW),
                // Только описание
                Arguments.of("Task", Priorities.MEDIUM, Statuses.ACTIVE,
                        null, null, "New Description",
                        "Task", Priorities.MEDIUM)
        );
    }

    @ParameterizedTest
    @MethodSource("changeTaskStatusProvider")
    @Transactional
    void testChangeTaskStatus(
            LocalDate deadline,
            Statuses initialStatus,
            Statuses expectedStatus
    ) throws Exception {
        TaskEntity task = new TaskEntity();
        task.setName("Тест статуса");
        task.setDeadline(deadline);
        task.setStatus(initialStatus);
        TaskEntity savedTask = taskRepository.save(task);

        mockMvc.perform(MockMvcRequestBuilders.patch("/mark/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expectedStatus.name()));

        assertEquals(expectedStatus,
                taskRepository.findById(savedTask.getId()).orElseThrow().getStatus());
    }

    private static Stream<Arguments> changeTaskStatusProvider() {
        return Stream.of(
                Arguments.of("2025-05-21", Statuses.ACTIVE, Statuses.COMPLETED),
                Arguments.of("2025-05-21", Statuses.COMPLETED, Statuses.ACTIVE),
                Arguments.of("2025-05-19", Statuses.OVERDUE, Statuses.LATE),
                Arguments.of("2025-05-19", Statuses.LATE, Statuses.OVERDUE)
        );
    }

    @Test
    @Transactional
    void testDeleteTaskSuccessfully() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setName("Удаляемая задача");
        task.setPriority(Priorities.MEDIUM);
        TaskEntity savedTask = taskRepository.save(task);

        mockMvc.perform(MockMvcRequestBuilders.delete("/delete/" + savedTask.getId()))
                .andExpect(status().isOk());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void testDeleteNonExistingTask() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks" + nonExistingId))
                .andExpect(status().isNotFound());
    }
}