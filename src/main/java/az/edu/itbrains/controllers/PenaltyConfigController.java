package az.edu.itbrains.controllers;

import az.edu.itbrains.DTOs.request.PenaltyConfigRequestDTO;
import az.edu.itbrains.DTOs.response.PenaltyConfigResponseDTO;
import az.edu.itbrains.services.PenaltyConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalty-config")
@RequiredArgsConstructor
public class PenaltyConfigController {

    private final PenaltyConfigService penaltyConfigService;

    @GetMapping
    public ResponseEntity<PenaltyConfigResponseDTO> getActiveConfig() {
        return ResponseEntity.ok(penaltyConfigService.getActiveConfig());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyConfigResponseDTO> createOrUpdateConfig(
            @Valid @RequestBody PenaltyConfigRequestDTO request) {
        return ResponseEntity.ok(penaltyConfigService.createOrUpdateConfig(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PenaltyConfigResponseDTO> getConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(penaltyConfigService.getConfigById(id));
    }
}
