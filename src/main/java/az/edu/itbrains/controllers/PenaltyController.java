package az.edu.itbrains.controllers;

import az.edu.itbrains.DTOs.request.PenaltyWaiveRequestDTO;
import az.edu.itbrains.DTOs.request.TaskCompletionRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyResponseDTO;
import az.edu.itbrains.DTOs.response.UserPenaltySummaryDTO;
import az.edu.itbrains.services.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;

    // ============ USER ENDPOINTLƏRİ ============

    @GetMapping("/my-penalties")
    public ResponseEntity<List<PenaltyResponseDTO>> getMyPenalties() {
        return ResponseEntity.ok(penaltyService.getCurrentUserPenalties());
    }

    @GetMapping("/my-summary")
    public ResponseEntity<UserPenaltySummaryDTO> getMyPenaltySummary() {
        return ResponseEntity.ok(penaltyService.getCurrentUserPenaltySummary());
    }

    @PostMapping("/complete-task")
    public ResponseEntity<String> completeTask(@Valid @RequestBody TaskCompletionRequestDTO request) {
        penaltyService.validateTaskCompletion(request.getTaskId(), request);
        return ResponseEntity.ok("Task uğurla tamamlandı");
    }

    // ============ ADMIN ENDPOINTLƏRİ ============

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PenaltyResponseDTO>> getAllPendingPenalties() {
        return ResponseEntity.ok(penaltyService.getAllPendingPenalties());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PenaltyResponseDTO>> getUserPenalties(@PathVariable Long userId) {
        return ResponseEntity.ok(penaltyService.getUserPenalties(userId));
    }

    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPenaltySummaryDTO> getUserPenaltySummary(@PathVariable Long userId) {
        return ResponseEntity.ok(penaltyService.getUserPenaltySummary(userId));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PenaltyResponseDTO>> getTaskPenalties(@PathVariable Long taskId) {
        return ResponseEntity.ok(penaltyService.getTaskPenalties(taskId));
    }

    @PostMapping("/waive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyResponseDTO> waivePenalty(@Valid @RequestBody PenaltyWaiveRequestDTO request) {
        return ResponseEntity.ok(penaltyService.waivePenalty(request));
    }

    @PostMapping("/{penaltyId}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyResponseDTO> markPenaltyAsPaid(@PathVariable Long penaltyId) {
        return ResponseEntity.ok(penaltyService.markPenaltyAsPaid(penaltyId));
    }
}
