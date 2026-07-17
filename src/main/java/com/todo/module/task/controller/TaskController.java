package com.todo.module.task.controller;

import com.todo.dto.ApiResponse;
import com.todo.module.task.dto.TaskDto;
import com.todo.module.task.dto.TaskRequest;
import com.todo.module.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gestion des tâches de l'utilisateur authentifié")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Lister les tâches paginées de l'utilisateur authentifié")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getTasks(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasks(pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    // @GetMapping("/{id}")
    // @Operation(summary = "Obtenir le détail d'une tâche")
    // public ResponseEntity<ApiResponse<TaskDto>> getTask(@PathVariable Long id) {
    //     TaskDto task = taskService.getTask(id);
    //     return ResponseEntity.ok(ApiResponse.success(task));
    // }

    @PostMapping
    @Operation(summary = "Créer une nouvelle tâche")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@Valid @RequestBody TaskRequest request) {
        TaskDto task = taskService.createTask(request);
        return ResponseEntity.ok(ApiResponse.success(task, "Tâche créée avec succès"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une tâche existante")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(@PathVariable Long id,
                                                             @Valid @RequestBody TaskRequest request) {
        TaskDto task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(task, "Tâche mise à jour avec succès"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une tâche")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tâche supprimée avec succès"));
    }
}
