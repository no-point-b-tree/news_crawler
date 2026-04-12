import request from '../request';

export const rssLinkApi = {
  pageRssLinks(params) {
    return request({
      url: '/manager/rss-links/page',
      method: 'post',
      data: params
    });
  },

  createRssLink(data) {
    return request({
      url: '/manager/rss-links',
      method: 'post',
      data
    });
  },

  updateRssLink(id, data) {
    return request({
      url: `/manager/rss-links/${id}`,
      method: 'put',
      data
    });
  },

  deleteRssLink(id) {
    return request({
      url: `/manager/rss-links/${id}`,
      method: 'delete'
    });
  },

  startRssTask(url, startDate, endDate) {
    return request({
      url: '/manager/rss-links/start-task',
      method: 'post',
      data: { url, startDate, endDate }
    });
  }
};