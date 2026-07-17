package com.todo.module.task.mapper;

import com.todo.module.task.dto.TaskDto;
import com.todo.module.task.dto.TaskRequest;
import com.todo.module.task.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    TaskDto toDto(Task task);

    Task toEntity(TaskRequest request);

    void updateEntityFromRequest(TaskRequest request, @MappingTarget Task task);
}
