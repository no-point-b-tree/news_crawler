# Vue3 + Element Plus + Axios 前端工程规范

## 1. 项目结构规范

### 标准项目结构
```
src/
├── api/                    # API接口层
│   ├── modules/           # 按模块划分的API
│   │   ├── newsLink.js    # 新闻链接API
│   │   ├── taskLog.js     # 任务日志API
│   │   └── cssSelector.js # CSS选择器API
│   ├── index.js           # API统一导出
│   ├── request.js         # Axios请求封装
│   └── constants.js       # API常量
├── components/            # 公共组件
│   ├── common/           # 通用组件
│   │   ├── SearchBar.vue
│   │   ├── Pagination.vue
│   │   └── Table.vue
│   └── layout/           # 布局组件
│       ├── Header.vue
│       ├── Sidebar.vue
│       └── Footer.vue
├── views/                 # 页面视图层
│   ├── news-link/        # 新闻链接模块
│   │   ├── NewsLinkList.vue
│   │   ├── NewsLinkForm.vue
│   │   └── NewsLinkDetail.vue
│   ├── task-log/         # 任务日志模块
│   │   └── TaskLogList.vue
│   └── css-selector/     # CSS选择器模块
│       └── CssSelectorList.vue
├── router/               # 路由配置
│   ├── index.js
│   └── modules/         # 路由模块
├── store/                # Vuex状态管理
│   ├── index.js
│   ├── modules/         # 模块化store
│   └── types.js         # Mutation/Action类型
├── utils/                # 工具函数
│   ├── request.js       # 请求工具
│   ├── validate.js      # 表单验证
│   ├── date.js          # 日期处理
│   └── common.js        # 通用函数
├── styles/               # 样式文件
│   ├── index.scss       # 全局样式
│   ├── variables.scss   # 变量定义
│   └── mixins.scss      # 混合宏
├── assets/               # 静态资源
│   ├── images/
│   ├── icons/
│   └── fonts/
└── App.vue               # 根组件
```

## 2. API层规范（视图接口请求分离）

### 2.1 Axios请求封装
```javascript
// src/api/request.js
import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录');
          router.push('/login');
          break;
        case 403:
          ElMessage.error('拒绝访问');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          ElMessage.error('服务器内部错误');
          break;
        default:
          ElMessage.error(error.response.data?.message || '请求失败');
      }
    } else {
      ElMessage.error('网络连接失败');
    }
    return Promise.reject(error);
  }
);

export default service;
```

### 2.2 模块化API定义
```javascript
// src/api/modules/newsLink.js
import request from '../request';

// 新闻链接API
export const newsLinkApi = {
  // 分页查询新闻链接
  pageNewsLinks(params) {
    return request({
      url: '/api/manager/news-links/page',
      method: 'get',
      params
    });
  },

  // 获取所有语言
  getAllLanguages() {
    return request({
      url: '/api/manager/news-links/languages',
      method: 'get'
    });
  },

  // 更新新闻链接
  updateNewsLink(id, data) {
    return request({
      url: `/api/manager/news-links/${id}`,
      method: 'put',
      data
    });
  },

  // 删除新闻链接
  deleteNewsLink(id) {
    return request({
      url: `/api/manager/news-links/${id}`,
      method: 'delete'
    });
  },

  // 开启单个任务
  startSingleTask(url) {
    return request({
      url: `/api/manager/news-links/${url}/start-task`,
      method: 'post'
    });
  },

  // 分页查询任务日志
  pageTaskLogsByUrl(url, params) {
    return request({
      url: `/api/manager/news-links/${url}/task-logs`,
      method: 'get',
      params
    });
  },

  // 分页查询CSS选择器
  pageCssSelectorsByUrl(url, params) {
    return request({
      url: `/api/manager/news-links/${url}/css-selectors`,
      method: 'get',
      params
    });
  },

  // 添加CSS选择器
  addCssSelector(url, cssSelector) {
    return request({
      url: `/api/manager/news-links/${url}/css-selectors`,
      method: 'post',
      params: { cssSelector }
    });
  },

  // 更新CSS选择器
  updateCssSelector(id, cssSelector) {
    return request({
      url: `/api/manager/news-links/css-selectors/${id}`,
      method: 'put',
      params: { cssSelector }
    });
  },

  // 删除CSS选择器
  deleteCssSelector(id) {
    return request({
      url: `/api/manager/news-links/css-selectors/${id}`,
      method: 'delete'
    });
  }
};

// CSS选择器关联API
export const cssSelectorApi = {
  // 分页查询CSS选择器关联
  pageCssSelectors(params) {
    return request({
      url: '/api/manager/css-selector-relations/page',
      method: 'get',
      params
    });
  },

  // 根据URL查询CSS选择器
  findByUrl(url) {
    return request({
      url: '/api/manager/css-selector-relations/by-url',
      method: 'get',
      params: { url }
    });
  },

  // 根据CSS选择器查询URL
  findByCssSelector(cssSelector) {
    return request({
      url: '/api/manager/css-selector-relations/by-css-selector',
      method: 'get',
      params: { cssSelector }
    });
  },

  // 保存或更新关联关系
  saveOrUpdateRelation(url, cssSelector) {
    return request({
      url: '/api/manager/css-selector-relations',
      method: 'post',
      params: { url, cssSelector }
    });
  },

  // 根据URL删除关联
  deleteByUrl(url) {
    return request({
      url: '/api/manager/css-selector-relations/by-url',
      method: 'delete',
      params: { url }
    });
  },

  // 根据CSS选择器删除关联
  deleteByCssSelector(cssSelector) {
    return request({
      url: '/api/manager/css-selector-relations/by-css-selector',
      method: 'delete',
      params: { cssSelector }
    });
  },

  // 根据URL获取CSS选择器列表
  findCssSelectorsByUrl(url) {
    return request({
      url: '/api/manager/css-selector-relations/css-selectors/by-url',
      method: 'get',
      params: { url }
    });
  },

  // 根据CSS选择器获取URL列表
  findUrlsByCssSelector(cssSelector) {
    return request({
      url: '/api/manager/css-selector-relations/urls/by-css-selector',
      method: 'get',
      params: { cssSelector }
    });
  }
};
```

### 2.3 API统一导出
```javascript
// src/api/index.js
export * from './modules/newsLink';
export * from './modules/cssSelector';
```

## 3. 组件规范

### 3.1 视图组件模板（视图与逻辑分离）
```vue
<!-- src/views/news-link/NewsLinkList.vue -->
<template>
  <div class="news-link-list">
    <!-- 搜索区域 -->
    <search-bar
      :keyword="searchParams.keyword"
      :langs="searchParams.langs"
      :languages="languages"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <data-table
      :data="tableData"
      :loading="loading"
      :selection="selection"
      @selection-change="handleSelectionChange"
      @edit="handleEdit"
      @delete="handleDelete"
      @start-task="handleStartTask"
    />

    <!-- 分页组件 -->
    <pagination
      :current-page="pagination.current"
      :page-size="pagination.size"
      :total="pagination.total"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { newsLinkApi } from '@/api';
import SearchBar from './components/SearchBar.vue';
import DataTable from './components/DataTable.vue';
import Pagination from '@/components/common/Pagination.vue';

// 响应式数据
const searchParams = reactive({
  keyword: '',
  langs: []
});

const tableData = ref([]);
const loading = ref(false);
const selection = ref([]);
const languages = ref([]);
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 生命周期
onMounted(() => {
  fetchLanguages();
  fetchData();
});

// API调用方法
const fetchLanguages = async () => {
  try {
    const res = await newsLinkApi.getAllLanguages();
    languages.value = res.data;
  } catch (error) {
    console.error('获取语言列表失败:', error);
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchParams,
      page: pagination.current,
      size: pagination.size
    };
    const res = await newsLinkApi.pageNewsLinks(params);
    tableData.value = res.data.records;
    pagination.total = res.data.total;
  } catch (error) {
    console.error('获取数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 事件处理方法
const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleReset = () => {
  searchParams.keyword = '';
  searchParams.langs = [];
  pagination.current = 1;
  fetchData();
};

const handlePageChange = (page) => {
  pagination.current = page;
  fetchData();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.current = 1;
  fetchData();
};

const handleSelectionChange = (val) => {
  selection.value = val;
};

const handleAdd = () => {
  // 跳转到新增页面
};

const handleEdit = (row) => {
  // 跳转到编辑页面
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该新闻链接吗？', '提示', {
      type: 'warning'
    });
    await newsLinkApi.deleteNewsLink(row.id);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleStartTask = async (row) => {
  try {
    await newsLinkApi.startSingleTask(row.url);
    ElMessage.success('任务已启动');
  } catch (error) {
    ElMessage.error('启动任务失败');
  }
};

const handleBatchDelete = async () => {
  if (selection.value.length === 0) {
    ElMessage.warning('请选择要删除的数据');
    return;
  }
  try {
    await ElMessageBox.confirm('确定删除选中的数据吗？', '提示', {
      type: 'warning'
    });
    const ids = selection.value.map(item => item.id);
    // 批量删除逻辑
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};
</script>

<style scoped>
.news-link-list {
  padding: 20px;
}

.action-buttons {
  margin: 20px 0;
}
</style>
```

### 3.2 展示组件（无业务逻辑）
```vue
<!-- src/views/news-link/components/DataTable.vue -->
<template>
  <el-table
    :data="data"
    :loading="loading"
    @selection-change="$emit('selection-change', $event)"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column prop="name" label="名称" min-width="120" />
    <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
    <el-table-column prop="lang" label="语言" width="100" />
    <el-table-column prop="domain" label="域名" width="150" />
    <el-table-column prop="createTime" label="创建时间" width="180">
      <template #default="{ row }">
        {{ formatDate(row.createTime) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" fixed="right">
      <template #default="{ row }">
        <el-button type="primary" size="small" @click="$emit('edit', row)">
          编辑
        </el-button>
        <el-button type="success" size="small" @click="$emit('start-task', row)">
          执行任务
        </el-button>
        <el-button type="danger" size="small" @click="$emit('delete', row)">
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { formatDate } from '@/utils/date';

defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
});

defineEmits(['selection-change', 'edit', 'delete', 'start-task']);
</script>
```

## 4. 状态管理规范

### 4.1 Vuex模块化
```javascript
// src/store/modules/newsLink.js
const state = {
  newsLinks: [],
  languages: [],
  pagination: {
    current: 1,
    size: 10,
    total: 0
  },
  searchParams: {
    keyword: '',
    langs: []
  }
};

const mutations = {
  SET_NEWS_LINKS(state, data) {
    state.newsLinks = data.records;
    state.pagination.total = data.total;
  },
  SET_LANGUAGES(state, languages) {
    state.languages = languages;
  },
  SET_PAGINATION(state, pagination) {
    state.pagination = { ...state.pagination, ...pagination };
  },
  SET_SEARCH_PARAMS(state, params) {
    state.searchParams = { ...state.searchParams, ...params };
  }
};

const actions = {
  async fetchNewsLinks({ commit, state }) {
    try {
      const params = {
        ...state.searchParams,
        page: state.pagination.current,
        size: state.pagination.size
      };
      const res = await newsLinkApi.pageNewsLinks(params);
      commit('SET_NEWS_LINKS', res.data);
    } catch (error) {
      console.error('获取新闻链接失败:', error);
    }
  },
  async fetchLanguages({ commit }) {
    try {
      const res = await newsLinkApi.getAllLanguages();
      commit('SET_LANGUAGES', res.data);
    } catch (error) {
      console.error('获取语言列表失败:', error);
    }
  }
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
};
```

## 5. 路由规范

### 5.1 路由配置
```javascript
// src/router/modules/newsLink.js
const routes = [
  {
    path: '/news-link',
    name: 'NewsLink',
    component: () => import('@/views/news-link/NewsLinkList.vue'),
    meta: {
      title: '新闻链接管理',
      icon: 'link'
    }
  },
  {
    path: '/news-link/add',
    name: 'NewsLinkAdd',
    component: () => import('@/views/news-link/NewsLinkForm.vue'),
    meta: {
      title: '新增新闻链接',
      hidden: true
    }
  },
  {
    path: '/news-link/edit/:id',
    name: 'NewsLinkEdit',
    component: () => import('@/views/news-link/NewsLinkForm.vue'),
    meta: {
      title: '编辑新闻链接',
      hidden: true
    }
  }
];

export default routes;
```

## 6. 样式规范

### 6.1 SCSS变量定义
```scss
// src/styles/variables.scss
$--color-primary: #409EFF;
$--color-success: #67C23A;
$--color-warning: #E6A23C;
$--color-danger: #F56C6C;
$--color-info: #909399;

$--font-size-base: 14px;
$--font-size-small: 12px;
$--font-size-large: 16px;

$--border-radius-base: 4px;
$--border-radius-small: 2px;
$--border-radius-round: 20px;

$--box-shadow-base: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
$--box-shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
```

### 6.2 组件样式
```scss
// src/views/news-link/styles/index.scss
.news-link-list {
  .search-bar {
    margin-bottom: 20px;
    padding: 20px;
    background: #fff;
    border-radius: $--border-radius-base;
    box-shadow: $--box-shadow-base;
  }

  .action-buttons {
    margin: 20px 0;
    display: flex;
    gap: 10px;
  }

  .data-table {
    margin-bottom: 20px;
    background: #fff;
    border-radius: $--border-radius-base;
    box-shadow: $--box-shadow-base;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    padding: 20px;
    background: #fff;
    border-radius: $--border-radius-base;
    box-shadow: $--box-shadow-base;
  }
}
```

## 7. 工具函数规范

### 7.1 请求工具
```javascript
// src/utils/request.js
export const createRequest = (api, options = {}) => {
  return async (params = {}, config = {}) => {
    try {
      const response = await api(params, { ...options, ...config });
      return response.data;
    } catch (error) {
      throw error;
    }
  };
};

export const debounce = (fn, delay = 300) => {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

export const throttle = (fn, delay = 300) => {
  let canRun = true;
  return function(...args) {
    if (!canRun) return;
    canRun = false;
    setTimeout(() => {
      fn.apply(this, args);
      canRun = true;
    }, delay);
  };
};
```

### 7.2 表单验证
```javascript
// src/utils/validate.js
export const validateRequired = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('该字段为必填项'));
  } else {
    callback();
  }
};

export const validateUrl = (rule, value, callback) => {
  const urlPattern = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/;
  if (!value || urlPattern.test(value)) {
    callback();
  } else {
    callback(new Error('请输入有效的URL地址'));
  }
};

export const validateEmail = (rule, value, callback) => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!value || emailPattern.test(value)) {
    callback();
  } else {
    callback(new Error('请输入有效的邮箱地址'));
  }
};
```

## 8. 代码质量规范

### 8.1 ESLint配置
```json
{
  "extends": [
    "eslint:recommended",
    "plugin:vue/vue3-recommended",
    "@vue/typescript/recommended"
  ],
  "rules": {
    "vue/multi-word-component-names": "off",
    "vue/no-v-html": "off",
    "vue/require-default-prop": "off",
    "@typescript-eslint/no-explicit-any": "off",
    "@typescript-eslint/explicit-module-boundary-types": "off",
    "no-console": process.env.NODE_ENV === "production" ? "warn" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "warn" : "off"
  }
}
```

### 8.2 Prettier配置
```json
{
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "semi": true,
  "singleQuote": true,
  "trailingComma": "none",
  "bracketSpacing": true,
  "arrowParens": "avoid",
  "endOfLine": "lf"
}
```

## 9. 最佳实践

### 9.1 组件设计原则
1. **单一职责**：每个组件只负责一个功能
2. **可复用性**：提取公共组件，避免重复代码
3. **可维护性**：清晰的props和emit定义
4. **可测试性**：组件逻辑与视图分离，便于单元测试

### 9.2 性能优化
1. **组件懒加载**：使用 `defineAsyncComponent` 或路由懒加载
2. **列表虚拟滚动**：大数据列表使用虚拟滚动
3. **图片懒加载**：使用 `v-lazy` 指令
4. **代码分割**：合理使用Webpack的代码分割

### 9.3 安全规范
1. **XSS防护**：避免使用 `v-html`，必须使用时进行过滤
2. **CSRF防护**：请求头添加CSRF Token
3. **输入验证**：前后端双重验证
4. **权限控制**：路由级别和组件级别的权限控制

---

**版本记录：**
- v1.0.0 (2024-01-01): 初始版本
- 技术栈：Vue3 + Element Plus + Axios
- 构建工具：Vite
- 包管理器：pnpm
- 代码规范：ESLint + Prettier
