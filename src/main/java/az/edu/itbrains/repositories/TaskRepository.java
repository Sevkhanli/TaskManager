package az.edu.itbrains.repositories;

import az.edu.itbrains.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Bu metod Spring-ə deyir ki:
    // "Get bazadan bütün taskları, amma yalnız deleted sahəsi false olanları gətir."
    List<Task> findByDeletedFalse();
}