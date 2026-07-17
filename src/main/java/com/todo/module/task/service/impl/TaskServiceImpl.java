package com.todo.module.task.service.impl;

import com.todo.exception.ForbiddenException;
import com.todo.exception.ResourceNotFoundException;
import com.todo.module.task.dto.TaskDto;
import com.todo.module.task.dto.TaskRequest;
import com.todo.module.task.mapper.TaskMapper;
import com.todo.module.task.model.Task;
import com.todo.module.task.model.TaskStatus;
import com.todo.module.task.repository.TaskRepository;
import com.todo.module.task.service.TaskService;
import com.todo.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable pageable) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        return taskRepository.findByUserId(userId, pageable).map(taskMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTask(Long id) {
        Task task = findOwnedTask(id);
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public TaskDto createTask(TaskRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        Task task = taskMapper.toEntity(request);
        task.setUserId(userId);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long id, TaskRequest request) {
        Task task = findOwnedTask(id);
        taskMapper.updateEntityFromRequest(request, task);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = findOwnedTask(id);
        taskRepository.delete(task);
    }

    private Task findOwnedTask(Long id) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'id : " + id));
        if (!task.getUserId().equals(userId)) {
            throw new ForbiddenException("Vous n'êtes pas autorisé à accéder à cette tâche");
        }
        return task;
    }
}
