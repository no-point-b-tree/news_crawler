import request from '../request';

export const cssSelectorApi = {
  pageCssSelectors(params) {
    return request({
      url: '/manager/css-selector-relations/page',
      method: 'get',
      params
    });
  },

  findByUrl(url) {
    return request({
      url: '/manager/css-selector-relations/by-url',
      method: 'get',
      params: { url }
    });
  },

  findByCssSelector(cssSelector) {
    return request({
      url: '/manager/css-selector-relations/by-css-selector',
      method: 'get',
      params: { cssSelector }
    });
  },

  saveOrUpdateRelation(url, cssSelector) {
    return request({
      url: '/manager/css-selector-relations',
      method: 'post',
      params: { url, cssSelector }
    });
  },

  deleteByUrl(url) {
    return request({
      url: '/manager/css-selector-relations/by-url',
      method: 'delete',
      params: { url }
    });
  },

  deleteByCssSelector(cssSelector) {
    return request({
      url: '/manager/css-selector-relations/by-css-selector',
      method: 'delete',
      params: { cssSelector }
    });
  },

  findCssSelectorsByUrl(url) {
    return request({
      url: '/manager/css-selector-relations/css-selectors/by-url',
      method: 'get',
      params: { url }
    });
  },

  findUrlsByCssSelector(cssSelector) {
    return request({
      url: '/manager/css-selector-relations/urls/by-css-selector',
      method: 'get',
      params: { cssSelector }
    });
  }
};