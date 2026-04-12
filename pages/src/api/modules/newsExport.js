import request from '../request';

export const newsExportApi = {
  /**
   * 导出新闻数据
   * @param {Object} params 导出参数
   * @param {string} params.exportFormat 导出格式: 'pdf' 或 'json'
   * @param {boolean} params.onlyRelevant 是否只导出相关新闻
   * @param {string} params.status 状态筛选
   * @param {number} params.taskId 任务ID筛选
   * @param {string} params.startTime 开始时间
   * @param {string} params.endTime 结束时间
   * @param {string} params.keyword 关键词搜索
   * @param {string} params.sourceType 来源类型筛选
   */
  exportNews(params) {
    return request({
      url: '/manager/news-export/export',
      method: 'post',
      data: params,
      responseType: 'blob' // 重要：用于文件下载
    });
  },

  /**
   * 预览导出数据
   */
  previewExportData(params) {
    return request({
      url: '/manager/news-export/preview',
      method: 'post',
      data: params
    });
  },

  /**
   * 测试导出服务
   */
  testExport() {
    return request({
      url: '/manager/news-export/test',
      method: 'get'
    });
  }
};