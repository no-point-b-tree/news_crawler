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
  },

  getTaskDetails(taskId) {
    return request({
      url: `/manager/task-logs/${taskId}/details`,
      method: 'get'
    });
  },

  getWebsiteNews(params) {
    return request({
      url: '/manager/task-logs/website-news',
      method: 'post',
      data: params
    });
  }
};