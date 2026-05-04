package az.edu.itbrains.services;

import az.edu.itbrains.DTOs.request.TaskRequestDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import java.util.List;

public interface TaskService {
    TaskResponseDTO createMyTask(TaskRequestDTO request);
    TaskResponseDTO createTaskAsAdmin(TaskRequestDTO request);
    List<TaskResponseDTO> getAllActiveTasks();
    TaskResponseDTO getTaskById(Long id);
    TaskResponseDTO updateTask(Long id, TaskRequestDTO request);
    TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason);
    String deleteTask(Long taskId);
}