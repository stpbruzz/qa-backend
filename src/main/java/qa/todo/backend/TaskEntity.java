package qa.todo.backend;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 4, message = "минимальная длина названия - 4 символа")
    private String name;

    private String description;
    private LocalDate deadline;

    @NotNull
    private Statuses status = Statuses.ACTIVE;

    @NotNull
    private Priorities priority;

    @NotNull
    @Column(updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDate creationDate = LocalDate.now();

    private LocalDate editedAt;

    protected void onUpdate() {
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

    public void updateStatus() {
        if (this.status == Statuses.COMPLETED || this.status == Statuses.LATE) {
            if (this.deadline != null && LocalDate.now().isAfter(this.deadline)) {
                this.status = Statuses.LATE;
            } else {
                this.status = Statuses.COMPLETED;
            }
            return;
        }

        if (this.deadline != null && LocalDate.now().isAfter(this.deadline)) {
            this.status = Statuses.OVERDUE;
        } else {
            this.status = Statuses.ACTIVE;
        }
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
        updateStatus();
    }
}
