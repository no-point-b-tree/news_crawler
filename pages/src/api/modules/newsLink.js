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
      url: `/manager/news-links/${encodeURIComponent(url)}/task-logs`,
      method: 'get',
      params
    });
  },

  pageCssSelectorsByUrl(url, params) {
    return request({
      url: `/manager/news-links/${encodeURIComponent(url)}/css-selectors`,
      method: 'get',
      params
    });
  },

  addCssSelector(url, cssSelector) {
    return request({
      url: `/manager/news-links/${encodeURIComponent(url)}/css-selectors`,
      method: 'post',
      params: { cssSelector }
    });
  },

  updateCssSelector(id, cssSelector) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'put',
      params: { cssSelector }
    });
  },

  deleteCssSelector(id) {
    return request({
      url: `/manager/news-links/css-selectors/${id}`,
      method: 'delete'
    });
  }
};