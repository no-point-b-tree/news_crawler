<template>
  <div class="news-link-list">
    <el-card class="search-card">
      <el-form :model="searchParams" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchParams.keyword"
            placeholder="请输入URL或名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="语言">
          <el-select
            v-model="searchParams.langs"
            multiple
            placeholder="请选择语言"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="lang in languages"
              :key="lang"
              :label="lang"
              :value="lang"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="action-card">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selection.length === 0">
        批量删除
      </el-button>
    </el-card>

    <el-card>
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="lang" label="语言" width="100" />
        <el-table-column prop="domain" label="域名" width="150" />
        <el-table-column prop="minLevel" label="最小等级" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleStartTask(row)">执行任务</el-button>
            <el-button type="info" size="small" @click="handleViewCssSelectors(row)">CSS选择器</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
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
    </el-card>

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
import { ref, reactive, onMounted } from 'vue';
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
    const res = await newsLinkApi.pageNewsLinks(params);
    tableData.value = res.records;
    pagination.total = res.total;
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
  ElMessage.info('新增功能待实现');
};

const handleEdit = (row) => {
  ElMessage.info('编辑功能待实现');
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

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
.news-link-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.action-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.css-selector-dialog .css-selector-form {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>