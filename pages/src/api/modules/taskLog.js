import request from '../request';

export const taskLogApi = {
  pageTaskLogs(params) {
    return request({
      url: '/manager/task-logs/page',
      method: 'post',
      data: params
    });
  },

  deleteTaskLog(id) {
    return request({
      url: `/manager/task-logs/${id}`,
      method: 'delete'
    });
  }
};