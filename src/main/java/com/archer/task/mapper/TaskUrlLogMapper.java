package com.archer.task.mapper;

import com.archer.task.entity.TaskUrlLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务URL处理日志Mapper
 */
@Mapper
public interface TaskUrlLogMapper extends BaseMapper<TaskUrlLog> {
}