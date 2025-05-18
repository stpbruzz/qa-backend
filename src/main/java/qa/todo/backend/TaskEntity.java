package qa.todo.backend;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 4, message = "минимальная длина названия - 4 символа")
    private String name;

    private String description;
    private LocalDate deadline;

    @NotNull
    @Column(updatable = false)
    private Statuses status = Statuses.ACTIVE;

    @NotNull
    private Priorities priority = Priorities.MEDIUM;

    @NotNull
    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDate creationDate = LocalDate.now();

    @Column(updatable = false)
    private LocalDate editedAt;

    @PreUpdate
    private void onUpdate() {
        this.editedAt = LocalDate.now();
        updateStatus();
    }

    public void markAsCompleted() {
        if (this.deadline != null && LocalDate.now().isAfter(this.deadline)) {
            this.status = Statuses.LATE;
        } else {
            this.status = Statuses.COMPLETED;
        }
    }

    public void markAsIncomplete() {
        if (this.deadline != null && LocalDate.now().isAfter(this.deadline)) {
            this.status = Statuses.OVERDUE;
        } else {
            this.status = Statuses.ACTIVE;
        }
    }

    private void updateStatus() {
        if (this.status == Statuses.COMPLETED || this.status == Statuses.LATE) {
            return;
        }

        if (this.deadline != null && LocalDate.now().isAfter(this.deadline)) {
            this.status = Statuses.OVERDUE;
        }
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
        updateStatus();
    }

    protected void setPriority(Priorities priority) {
        this.priority = priority;
    }
}
