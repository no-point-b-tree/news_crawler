import request from '../request';

export const newsLinkApi = {
  pageNewsLinks(params) {
    return request({
      url: '/manager/news-links/page',
      method: 'post',
      data: params
    });
  },

  pageNewsLinksWithCssCount(params) {
    return request({
      url: '/manager/news-links/page-with-css-count',
      method: 'post',
      data: params
    });
  },

  getAllLanguages() {
    return request({
      url: '/manager/news-links/languages',
      method: 'get'
    });
  },

  getLocaleLanguages() {
    return request({
      url: '/manager/news-links/locale-languages',
      method: 'get'
    });
  },

  createNewsLink(data) {
    return request({
      url: '/manager/news-links',
      method: 'post',
      data
    });
  },

  createNewsLink(data) {
    return request({
      url: '/manager/news-links',
      method: 'post',
      data
    });
  },

  updateNewsLink(id, data) {
    return request({
      url: `/manager/news-links/${id}`,
      method: 'put',
      data
    });
  },

  deleteNewsLink(id) {
    return request({
      url: `/manager/news-links/${id}`,
      method: 'delete'
    });
  },

  startSingleTask(url, timeRange) {
    return request({
      url: '/manager/news-links/start-task',
      method: 'post',
      data: { 
        url,
        ...(timeRange && { startDate: timeRange.startDate }),
        ...(timeRange && { endDate: timeRange.endDate })
      }
    });
  },

  pageTaskLogsByUrl(url, params) {
    return request({
      url: '/manager/news-links/task-logs/page',
      method: 'post',
      data: {
        url: url,
        ...params
      }
    });
  },

  pageCssSelectorsByUrl(url, params) {
    return request({
      url: '/manager/news-links/css-selectors/page',
      method: 'post',
      data: {
        url: url,
        ...params
      }
    });
  },

  addCssSelector(url, data) {
    return request({
      url: '/manager/news-links/css-selectors',
      method: 'post',
      data: {
        url: url,
        ...data
      }
    });
  },

  updateCssSelector(id, data) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'put',
      data
    });
  },

  findHyperLinkCount(url, timeRange) {
    return request({
      url: '/manager/news-links/hyper-link-count',
      method: 'post',
      data: {
        url,
        ...(timeRange && { startDate: timeRange.startDate }),
        ...(timeRange && { endDate: timeRange.endDate })
      }
    });
  },

  deleteCssSelector(id) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'delete'
    });
  }
};