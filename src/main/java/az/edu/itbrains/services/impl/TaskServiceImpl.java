package az.edu.itbrains.services.impl;

import az.edu.itbrains.DTOs.request.TaskRequestDTO;
import az.edu.itbrains.DTOs.response.TaskResponseDTO;
import az.edu.itbrains.enums.TaskStatus;
import az.edu.itbrains.models.Task;
import az.edu.itbrains.models.User;
import az.edu.itbrains.repositories.StatusHistoryRepository;
import az.edu.itbrains.repositories.TaskRepository;
import az.edu.itbrains.repositories.UserRepository;
import az.edu.itbrains.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository historyRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TaskResponseDTO createMyTask(TaskRequestDTO request) {
        User currentUser = getCurrentUser();
        Task task = modelMapper.map(request, Task.class);

        task.setId(null);
        task.setCreator(currentUser);
        task.setAssignee(currentUser);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeleted(false);

        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO createTaskAsAdmin(TaskRequestDTO request) {
        User creator = getCurrentUser();
        Task task = modelMapper.map(request, Task.class);

        task.setId(null);
        task.setCreator(creator);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setDeleted(false);

        if (request.getAssigneeId() == null) {
            throw new RuntimeException("Admin üçün assigneeId vacibdir!");
        }

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee tapılmadı"));
        task.setAssignee(assignee);

        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllActiveTasks() {
        return taskRepository.findByDeletedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        return convertToResponse(taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task tapılmadı")));
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        task.setStatus(newStatus);

        // Burada tarixçəyə əlavə etmək üçün history obyektini də saxlaya bilərsən

        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public String deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        task.setDeleted(true);
        taskRepository.save(task);

        return "Task ID: " + taskId + " uğurla silindi.";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    private TaskResponseDTO convertToResponse(Task task) {
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);
        if (task.getCreator() != null) dto.setCreatorName(task.getCreator().getFullName());
        if (task.getAssignee() != null) dto.setAssigneeName(task.getAssignee().getFullName());
        return dto;
    }
}