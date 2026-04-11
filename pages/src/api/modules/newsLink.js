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
      url: `/manager/news-links/${encodeURIComponent(url)}/start-task`,
      method: 'post'
    });
  },

  pageTaskLogsByUrl(url, params) {
    return request({
      url: `/manager/news-links/${encodeURIComponent(url)}/task-logs/page`,
      method: 'post',
      data: params
    });
  },

  pageCssSelectorsByUrl(url, params) {
    return request({
      url: `/manager/news-links/${encodeURIComponent(url)}/css-selectors/page`,
      method: 'post',
      data: params
    });
  },

  addCssSelector(url, cssSelector) {
    return request({
      url: `/manager/news-links/${encodeURIComponent(url)}/css-selectors`,
      method: 'post',
      data: { cssSelector }
    });
  },

  updateCssSelector(id, cssSelector) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'put',
      data: { cssSelector }
    });
  },

  deleteCssSelector(id) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'delete'
    });
  }
};