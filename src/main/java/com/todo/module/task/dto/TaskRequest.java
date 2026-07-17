package com.todo.module.task.dto;

import com.todo.module.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    private TaskStatus status;

    private LocalDate dueDate;
}
