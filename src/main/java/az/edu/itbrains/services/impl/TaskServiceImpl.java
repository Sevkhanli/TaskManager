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

    // --- YARADILMA METODLARI ---

    @Override
    @Transactional
    public TaskResponseDTO createMyTask(TaskRequestDTO request) {
        User currentUser = getCurrentUser();
        Task task = modelMapper.map(request, Task.class);
        task.setId(null);
        task.setCreator(currentUser);
        task.setAssignee(currentUser); // Özü yaradıbsa, icraçı da özüdür
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

        if (request.getAssigneeId() == null) throw new RuntimeException("Admin üçün assigneeId vacibdir!");

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Assignee tapılmadı"));
        task.setAssignee(assignee);
        return convertToResponse(taskRepository.save(task));
    }

    // --- OXUMA METODLARI ---

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllActiveTasks() {
        User currentUser = getCurrentUser();
        List<Task> tasks;

        if (isAdmin()) {
            tasks = taskRepository.findByDeletedFalse();
        } else {
            tasks = taskRepository.findTasksByUserId(currentUser.getId());
        }

        return tasks.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        // Admin, Creator və ya Assignee deyilsə görə bilməz
        if (!isAdmin() && !isCreator(task) && !isAssignee(task)) {
            throw new RuntimeException("Bu taskı görməyə icazəniz yoxdur!");
        }
        return convertToResponse(task);
    }

    // --- YENİLƏNMƏ METODLARI (TƏHLÜKƏSİZLİK BURADA) ---

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        // QAYDA: Yalnız Admin və ya Creator (yaratdığı şəxs) update edə bilər.
        // Assignee (icraçı) title/deadline-ı dəyişə bilməz!
        if (!isAdmin() && !isCreator(task)) {
            throw new RuntimeException("Bu taskı redaktə etməyə icazəniz yoxdur!");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponseDTO changeStatus(Long taskId, TaskStatus newStatus, String reason) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        // QAYDA: Admin, Creator VƏ YA Assignee statusu dəyişə bilər.
        if (!isAdmin() && !isCreator(task) && !isAssignee(task)) {
            throw new RuntimeException("Statusu dəyişməyə icazəniz yoxdur!");
        }

        task.setStatus(newStatus);
        return convertToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public String deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task tapılmadı"));

        // SİLMƏ: Admin və ya Creator silə bilər
        if (!isAdmin() && !isCreator(task)) {
            throw new RuntimeException("Bu taskı silməyə icazəniz yoxdur!");
        }

        task.setDeleted(true);
        taskRepository.save(task);
        return "Task ID: " + taskId + " uğurla silindi.";
    }

    // --- KÖMƏKÇİ METODLAR (DRY - Don't Repeat Yourself) ---

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isCreator(Task task) {
        return task.getCreator().getId().equals(getCurrentUser().getId());
    }

    private boolean isAssignee(Task task) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(getCurrentUser().getId());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    private TaskResponseDTO convertToResponse(Task task) {
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);
        if (task.getCreator() != null) dto.setCreatorName(task.getCreator().getFullName());
        if (task.getAssignee() != null) dto.setAssigneeName(task.getAssignee().getFullName());
        return dto;
    }
}


//TODO Nəyi dəyişdik?
//todo//Helper Metodlar: isAdmin(), isCreator(), isAssignee() metodlarını yaratdıq. Kodun hər yerində eyni SecurityContextHolder yoxlanışını yazmaq əvəzinə bu qısa metodları istifadə edirik.
//
//updateTask: İndi burada isAssignee yoxdur. Yəni, user-ə tapşırılan taskın adını və ya deadline-ını dəyişə bilməz, yalnız Admin və ya yaradıcı bunu edə bilər.
//
//changeStatus: Burada isAssignee əlavə olunub. Yəni, bir user-ə tapşırıq verilibsə, o, tapşırığın məzmununu dəyişə bilməz, amma statusunu (Pending -> Done) dəyişə bilər.