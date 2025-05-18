package qa.todo.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {
    List<TaskEntity> findAllByOrderByCreationDateAsc();
    List<TaskEntity> findAllByOrderByCreationDateDesc();

    @Query("SELECT t FROM TaskEntity t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) " +
            "ORDER BY " +
            "CASE WHEN :sortOrder = 'newest' THEN t.creationDate END DESC, " +
            "CASE WHEN :sortOrder = 'oldest' THEN t.creationDate END ASC")
    List<TaskEntity> findFilteredTasks(
            @Param("status") Statuses status,
            @Param("priority") Priorities priority,
            @Param("sortOrder") String sortOrder);
}
