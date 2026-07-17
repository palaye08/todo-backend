package com.todo.module.task.service;

import com.todo.module.task.dto.TaskDto;
import com.todo.module.task.dto.TaskRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    Page<TaskDto> getTasks(Pageable pageable);

    TaskDto getTask(Long id);

    TaskDto createTask(TaskRequest request);

    TaskDto updateTask(Long id, TaskRequest request);

    void deleteTask(Long id);
}
