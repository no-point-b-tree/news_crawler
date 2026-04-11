<template>
  <div class="news-link-list">
    <el-container class="container">
      <el-main>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card class="search-card">
              <el-form :model="searchParams" :inline="true" class="search-form">
                <el-row :gutter="20" style="width: 100%">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="关键词">
                      <el-input
                        v-model="searchParams.keyword"
                        placeholder="请输入URL或名称"
                        clearable
                        @keyup.enter="handleSearch"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="语言">
                      <el-select
                        v-model="searchParams.langs"
                        multiple
                        placeholder="请选择语言"
                        clearable
                        style="width: 100%"
                      >
                        <el-option
                          v-for="lang in languages"
                          :key="lang"
                          :label="lang"
                          :value="lang"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="8" :lg="12">
                    <el-form-item>
                      <el-button type="primary" @click="handleSearch">查询</el-button>
                      <el-button @click="handleReset">重置</el-button>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-card>
          </el-col>

          <el-col :span="24">
            <el-card class="action-card">
              <el-button type="primary" @click="handleAdd">新增</el-button>
              <el-button type="danger" @click="handleBatchDelete" :disabled="selection.length === 0">
                批量删除
              </el-button>
            </el-card>
          </el-col>

          <el-col :span="24">
            <el-card>
              <el-table
                :data="tableData"
                v-loading="loading"
                @selection-change="handleSelectionChange"
                style="width: 100%"
                :flexible="true"
              >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="名称" min-width="120">
          <template #default="{ row }">
            <span class="text-truncate" :title="row.name">{{ truncateText(row.name, 20) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="200">
          <template #default="{ row }">
            <el-link
              :href="row.url"
              target="_blank"
              type="primary"
              :title="row.url"
              class="url-link"
            >
              {{ truncateText(row.url, 30) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="lang" label="语言" min-width="80" />
        <el-table-column prop="domain" label="域名" min-width="100">
          <template #default="{ row }">
            <span class="text-truncate" :title="row.domain">{{ truncateText(row.domain, 15) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minLevel" label="最小等级" min-width="90" />
        <el-table-column prop="createTime" label="创建时间" min-width="140">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="success" size="small" @click="handleStartTask(row)">执行任务</el-button>
              <el-button type="info" size="small" @click="handleViewCssSelectors(row)">CSS选择器</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
              </el-table>

              <div class="pagination" v-if="pagination.total > 0">
                <el-pagination
                  v-model:current-page="pagination.current"
                  v-model:page-size="pagination.size"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handlePageChange"
                />
              </div>
              <div class="pagination" v-else>
                <div class="no-data">暂无数据</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.title"
      width="600px"
      @close="handleFormClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入新闻链接名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="URL" prop="url">
          <el-input
            v-model="formData.url"
            placeholder="请输入完整的URL地址"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="域名" prop="domain">
          <el-input
            v-model="formData.domain"
            placeholder="请输入域名（自动从URL提取）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="语言" prop="lang">
          <el-select
            v-model="formData.lang"
            placeholder="请选择语言"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="lang in languages"
              :key="lang"
              :label="lang"
              :value="lang"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="最小等级" prop="minLevel">
          <el-input-number
            v-model="formData.minLevel"
            :min="0"
            :max="10"
            placeholder="请输入最小等级（0-10）"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleFormSubmit" :loading="formDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cssSelectorDialog.visible"
      :title="`${cssSelectorDialog.url} - CSS选择器管理`"
      width="800px"
    >
      <div class="css-selector-dialog">
        <div class="css-selector-form">
          <el-input
            v-model="cssSelectorDialog.newSelector"
            placeholder="请输入CSS选择器"
            style="width: 300px; margin-right: 10px"
          />
          <el-button type="primary" @click="handleAddCssSelector">添加</el-button>
        </div>

        <el-table :data="cssSelectorDialog.data" style="margin-top: 20px">
          <el-table-column prop="cssSelector" label="CSS选择器" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleDeleteCssSelector(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="cssSelectorDialog.pagination.current"
            v-model:page-size="cssSelectorDialog.pagination.size"
            :total="cssSelectorDialog.pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleCssSelectorSizeChange"
            @current-change="handleCssSelectorPageChange"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { newsLinkApi } from '@/api';

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

const cssSelectorDialog = reactive({
  visible: false,
  url: '',
  newSelector: '',
  data: [],
  pagination: {
    current: 1,
    size: 10,
    total: 0
  }
});

const formDialog = reactive({
  visible: false,
  title: '新增新闻链接',
  loading: false,
  isEdit: false
});

const formData = reactive({
  id: null,
  name: '',
  url: '',
  domain: '',
  lang: '',
  minLevel: 0
});

const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入URL', trigger: 'blur' },
    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  domain: [
    { required: true, message: '请输入域名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  lang: [
    { required: true, message: '请选择语言', trigger: 'change' }
  ],
  minLevel: [
    { type: 'number', min: 0, max: 10, message: '范围在 0 到 10 之间', trigger: 'blur' }
  ]
};

const formRef = ref();

onMounted(() => {
  fetchLanguages();
  fetchData();
});

const fetchLanguages = async () => {
  try {
    const res = await newsLinkApi.getAllLanguages();
    languages.value = res;
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
    console.log('请求参数:', params);
    const res = await newsLinkApi.pageNewsLinks(params);
    console.log('API响应:', res);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
    console.log('分页数据:', { records: tableData.value.length, total: pagination.total });
  } catch (error) {
    console.error('获取数据失败:', error);
  } finally {
    loading.value = false;
  }
};

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
  formDialog.title = '新增新闻链接';
  formDialog.isEdit = false;
  
  Object.assign(formData, {
    id: null,
    name: '',
    url: '',
    domain: '',
    lang: '',
    minLevel: 0
  });
  
  formDialog.visible = true;
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

const handleEdit = (row) => {
  formDialog.title = '编辑新闻链接';
  formDialog.isEdit = true;
  
  Object.assign(formData, {
    id: row.id,
    name: row.name || '',
    url: row.url || '',
    domain: row.domain || '',
    lang: row.lang || '',
    minLevel: row.minLevel || 0
  });
  
  formDialog.visible = true;
  
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

const handleFormClose = () => {
  formDialog.loading = false;
};

const handleFormSubmit = async () => {
  try {
    await formRef.value.validate();
    
    formDialog.loading = true;
    
    if (formDialog.isEdit) {
      await newsLinkApi.updateNewsLink(formData.id, formData);
      ElMessage.success('更新成功');
    } else {
      await newsLinkApi.createNewsLink(formData);
      ElMessage.success('创建成功');
    }
    
    formDialog.visible = false;
    fetchData();
  } catch (error) {
    if (error && error.errors) {
      ElMessage.error('请检查表单数据');
    } else {
      ElMessage.error(formDialog.isEdit ? '更新失败' : '创建失败');
    }
  } finally {
    formDialog.loading = false;
  }
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

watch(() => formData.url, (newUrl) => {
  if (newUrl && !formDialog.isEdit) {
    try {
      const urlObj = new URL(newUrl);
      formData.domain = urlObj.hostname;
    } catch (error) {
      formData.domain = '';
    }
  }
});

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
    for (const id of ids) {
      await newsLinkApi.deleteNewsLink(id);
    }
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleViewCssSelectors = async (row) => {
  cssSelectorDialog.visible = true;
  cssSelectorDialog.url = row.url;
  cssSelectorDialog.newSelector = '';
  cssSelectorDialog.pagination.current = 1;
  fetchCssSelectors();
};

const fetchCssSelectors = async () => {
  try {
    const params = {
      page: cssSelectorDialog.pagination.current,
      size: cssSelectorDialog.pagination.size
    };
    const res = await newsLinkApi.pageCssSelectorsByUrl(cssSelectorDialog.url, params);
    cssSelectorDialog.data = res.records;
    cssSelectorDialog.pagination.total = res.total;
  } catch (error) {
    console.error('获取CSS选择器失败:', error);
  }
};

const handleAddCssSelector = async () => {
  if (!cssSelectorDialog.newSelector.trim()) {
    ElMessage.warning('请输入CSS选择器');
    return;
  }
  try {
    await newsLinkApi.addCssSelector(cssSelectorDialog.url, cssSelectorDialog.newSelector);
    ElMessage.success('添加成功');
    cssSelectorDialog.newSelector = '';
    fetchCssSelectors();
  } catch (error) {
    ElMessage.error('添加失败');
  }
};

const handleDeleteCssSelector = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该CSS选择器吗？', '提示', {
      type: 'warning'
    });
    await newsLinkApi.deleteCssSelector(row.id);
    ElMessage.success('删除成功');
    fetchCssSelectors();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleCssSelectorPageChange = (page) => {
  cssSelectorDialog.pagination.current = page;
  fetchCssSelectors();
};

const handleCssSelectorSizeChange = (size) => {
  cssSelectorDialog.pagination.size = size;
  cssSelectorDialog.pagination.current = 1;
  fetchCssSelectors();
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
.news-link-list {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.container {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
}

.el-main {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
  width: 100%;
}

.search-form .el-input,
.search-form .el-select {
  width: 100%;
}

.action-card {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .action-buttons {
    flex-wrap: wrap;
    gap: 5px;
  }
  
  .action-buttons .el-button {
    flex: 1;
    min-width: 60px;
  }
}

.pagination {
  margin-top: 20px;
  padding: 15px 0;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  min-height: 60px;
  align-items: center;
}

.pagination .no-data {
  width: 100%;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .pagination {
    justify-content: center;
    padding: 10px 0;
  }
  
  .pagination .el-pagination {
    flex-wrap: wrap;
  }
}

.css-selector-dialog .css-selector-form {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .css-selector-dialog .css-selector-form {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .css-selector-dialog .css-selector-form .el-input {
    width: 100% !important;
    margin-right: 0 !important;
  }
}

.text-truncate {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.url-link {
  text-decoration: none;
}

.url-link:hover {
  text-decoration: underline;
}

@media (max-width: 1200px) {
.el-table {
  font-size: 12px;
}

.el-table .el-button {
  font-size: 12px;
  padding: 5px 8px;
}

.el-table .el-table__header-wrapper .el-table__cell {
  white-space: nowrap;
}

.el-table .el-table__body-wrapper .el-table__cell {
  white-space: nowrap;
}
}

@media (max-width: 992px) {
  .el-main {
    padding: 15px;
  }
}

.el-row {
  margin-bottom: 20px;
}

.el-col {
  margin-bottom: 20px;
}

.el-card__body {
  padding: 20px;
}
</style>