package com.archer.task.manager.controller;

import com.archer.task.entity.TaskLog;
import com.archer.task.manager.dto.PageRequest;
import com.archer.task.manager.dto.TaskLogPageRequest;
import com.archer.task.manager.service.TaskLogManagerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/task-logs")
@RequiredArgsConstructor
public class TaskLogManagerController {

    private final TaskLogManagerService taskLogManagerService;

    @PostMapping("/page")
    public ResponseEntity<Page<TaskLog>> pageTaskLogs(@RequestBody TaskLogPageRequest request) {
        Page<TaskLog> result = taskLogManagerService.pageTaskLogs(
            request.getTaskType(),
            request.getStartDate(),
            request.getEndDate(),
            request.getPage(),
            request.getSize()
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTaskLog(@PathVariable Long id) {
        boolean result = taskLogManagerService.deleteTaskLog(id);
        return ResponseEntity.ok(result);
    }
}