import request from '../request';

export const newsLinkApi = {
  pageNewsLinks(params) {
    return request({
      url: '/manager/news-links/page',
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

  startSingleTask(url) {
    return request({
      url: '/manager/news-links/start-task',
      method: 'post',
      data: { url }
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

  deleteCssSelector(id) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'delete'
    });
  }
};