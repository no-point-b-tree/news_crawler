<template>
  <div class="task-log-list">
    <el-container class="container">
      <el-main>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-card class="search-card">
              <el-form :model="searchParams" :inline="true" class="search-form">
                <el-row :gutter="20" style="width: 100%">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="任务类型">
                      <el-input
                        v-model="searchParams.taskType"
                        placeholder="请输入任务类型"
                        clearable
                        @keyup.enter="handleSearch"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="开始时间">
                      <el-date-picker
                        v-model="searchParams.startDate"
                        type="datetime"
                        placeholder="选择开始时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="结束时间">
                      <el-date-picker
                        v-model="searchParams.endDate"
                        type="datetime"
                        placeholder="选择结束时间"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                   <el-col :xs="24" :sm="24" :md="8" :lg="6">
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
            <el-card>
              <el-table
                :data="tableData"
                v-loading="loading"
                style="width: 100%"
                :flexible="true"
              >
                <el-table-column label="序号" width="80">
                  <template #default="{ $index }">
                    {{ (pagination.current - 1) * pagination.size + $index + 1 }}
                  </template>
                </el-table-column>
                <el-table-column prop="taskType" label="任务类型" min-width="120" />
                <el-table-column prop="startDate" label="开始时间" min-width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.startDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="endDate" label="结束时间" min-width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.endDate) }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="250" fixed="right">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-button type="primary" size="small" @click="handleViewDetails(row)">查看详情</el-button>
                      <el-dropdown @command="(command) => handleTaskExport(row, command)" size="small" style="margin-left: 5px;">
                        <el-button type="success" size="small">
                          导出<el-icon class="el-icon--right"><arrow-down /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="pdf">导出为PDF</el-dropdown-item>
                            <el-dropdown-item command="json">导出为JSON</el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
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
       v-model="taskDetailDialog.visible"
       :title="taskDetailDialog.title"
       width="900px"
       @close="handleTaskDetailClose"
     >
       <div v-loading="taskDetailDialog.loading">
         <!-- 网站分组统计 -->
         <el-card class="website-summary-card" v-if="taskDetailDialog.websiteData.length > 0">
           <template #header>
             <div class="card-header">
               <span>网站统计</span>
             </div>
           </template>
           <el-table :data="taskDetailDialog.websiteData" style="width: 100%">
             <el-table-column prop="name" label="网站名称" min-width="150" />
             <el-table-column prop="url" label="网站URL" min-width="200">
               <template #default="{ row }">
                 <el-link :href="row.url" target="_blank" type="primary" :title="row.url">
                   {{ truncateText(row.url, 30) }}
                 </el-link>
               </template>
             </el-table-column>
             <el-table-column prop="newsCount" label="新闻数量" width="100" />
             <el-table-column label="操作" width="120" fixed="right">
               <template #default="{ row }">
                 <el-button type="primary" size="small" @click="handleViewWebsiteNews(row)">查看新闻</el-button>
               </template>
             </el-table-column>
           </el-table>
         </el-card>

         <div v-else class="no-data">
           暂无网站数据
         </div>
       </div>
     </el-dialog>

      <!-- 网站新闻详情对话框 -->
      <el-dialog
        v-model="websiteNewsDialog.visible"
        :title="websiteNewsDialog.title"
        width="1200px"
        @close="handleWebsiteNewsClose"
      >
       <div v-loading="websiteNewsDialog.loading">
           <el-table :data="websiteNewsDialog.newsData" style="width: 100%">
              <el-table-column prop="zhTitle" label="中文标题" min-width="180">
                <template #default="{ row }">
                  <span class="text-truncate" :title="row.zhTitle || row.title">
                    {{ truncateText(row.zhTitle || row.title, 25) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="summary" label="摘要" min-width="200">
                <template #default="{ row }">
                  <span class="text-truncate" :title="row.summary">{{ truncateText(row.summary, 80) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="新闻内容" min-width="200">
                <template #default="{ row }">
                  <span class="text-truncate" :title="row.content">{{ truncateText(row.content, 80) }}</span>
                </template>
              </el-table-column>
             <el-table-column prop="status" label="状态" width="100">
               <template #default="{ row }">
                 <el-tag :type="getStatusTagType(row.status)" size="small">
                   {{ getStatusText(row.status) }}
                 </el-tag>
               </template>
             </el-table-column>
             <el-table-column prop="source" label="来源" width="120">
               <template #default="{ row }">
                 <span class="text-truncate" :title="row.source">{{ truncateText(row.source, 15) }}</span>
               </template>
             </el-table-column>
             <el-table-column prop="url" label="文章链接" width="120">
              <template #default="{ row }">
                <el-link
                  v-if="row.url"
                  :href="row.url"
                  target="_blank"
                  type="primary"
                  :title="row.url"
                >
                  查看原文
                </el-link>
                <span v-else class="no-link">无链接</span>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="150">
              <template #default="{ row }">
                {{ formatDate(row.publishTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="采集时间" width="150">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>

         <div class="pagination" v-if="websiteNewsDialog.pagination.total > 0">
           <el-pagination
             v-model:current-page="websiteNewsDialog.pagination.current"
             v-model:page-size="websiteNewsDialog.pagination.size"
             :total="websiteNewsDialog.pagination.total"
             :page-sizes="[10, 20, 50]"
             layout="total, sizes, prev, pager, next"
             @size-change="handleWebsiteNewsSizeChange"
             @current-change="handleWebsiteNewsPageChange"
           />
         </div>
        </div>
      </el-dialog>

      <!-- 高级导出对话框 -->
      <el-dialog
        v-model="exportDialog.visible"
        title="高级导出设置"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form
          :model="exportDialog.formData"
          label-width="100px"
          :disabled="exportDialog.loading"
        >
          <el-form-item label="导出格式">
            <el-radio-group v-model="exportDialog.formData.exportFormat">
              <el-radio label="pdf">PDF</el-radio>
              <el-radio label="json">JSON</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="导出范围">
            <el-checkbox v-model="exportDialog.formData.onlyRelevant">
              只导出相关新闻（IS_RELEVANT状态）
            </el-checkbox>
          </el-form-item>
          
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="exportDialog.formData.startTime"
              type="datetime"
              placeholder="选择开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="exportDialog.formData.endTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="状态筛选">
            <el-select
              v-model="exportDialog.formData.status"
              placeholder="选择状态"
              clearable
              style="width: 100%"
            >
              <el-option label="全部" value="" />
              <el-option label="爬取成功" value="SPIDER_SUCCESS" />
              <el-option label="具备相关性" value="IS_RELEVANT" />
              <el-option label="不相关" value="IS_NOT_RELEVANT" />
              <el-option label="AI处理失败" value="AI_DEAL_FAIL" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="关键词搜索">
            <el-input
              v-model="exportDialog.formData.keyword"
              placeholder="输入标题、内容或摘要关键词"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="来源类型">
            <el-select
              v-model="exportDialog.formData.sourceType"
              placeholder="选择来源类型"
              clearable
              style="width: 100%"
            >
              <el-option label="全部" value="" />
              <el-option label="RSS订阅" value="RSS" />
              <el-option label="新闻链接" value="NEWS" />
            </el-select>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="exportDialog.visible = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="handleExportSubmit"
              :loading="exportDialog.loading"
            >
              开始导出
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { taskLogApi, newsExportApi } from '@/api';

const searchParams = reactive({
  taskType: '',
  startDate: '',
  endDate: ''
});

const tableData = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

const taskDetailDialog = reactive({
  visible: false,
  title: '任务执行详情',
  loading: false,
  taskId: null,
  websiteData: []
});

const websiteNewsDialog = reactive({
  visible: false,
  title: '网站新闻详情',
  loading: false,
  sourceUrl: '',
  newsData: [],
  pagination: {
    current: 1,
    size: 10,
    total: 0
  }
});

onMounted(() => {
  fetchData();
});

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchParams,
      page: pagination.current,
      size: pagination.size
    };
    const res = await taskLogApi.pageTaskLogs(params);
    tableData.value = res.records || [];
    pagination.total = res.total || 0;
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
  searchParams.taskType = '';
  searchParams.startDate = '';
  searchParams.endDate = '';
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该任务日志吗？', '提示', {
      type: 'warning'
    });
    await taskLogApi.deleteTaskLog(row.id);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleViewDetails = async (row) => {
  try {
    taskDetailDialog.taskId = row.id;
    taskDetailDialog.title = `任务执行详情 - ID: ${row.id}`;
    taskDetailDialog.loading = true;
    taskDetailDialog.visible = true;
    
    // 获取任务详情数据
    const result = await taskLogApi.getTaskDetails(row.id);
    taskDetailDialog.websiteData = result.websites || [];
    
  } catch (error) {
    console.error('获取任务详情失败:', error);
    ElMessage.error('获取任务详情失败');
    taskDetailDialog.visible = false;
  } finally {
    taskDetailDialog.loading = false;
  }
};

const handleTaskDetailClose = () => {
  taskDetailDialog.visible = false;
  taskDetailDialog.taskId = null;
  taskDetailDialog.websiteData = [];
};

const handleViewWebsiteNews = async (row) => {
  try {
    websiteNewsDialog.sourceUrl = row.url;
    websiteNewsDialog.title = `网站新闻 - ${row.name}`;
    websiteNewsDialog.loading = true;
    websiteNewsDialog.visible = true;
    
    // 重置分页
    websiteNewsDialog.pagination.current = 1;
    
    await fetchWebsiteNews();
    
  } catch (error) {
    console.error('获取网站新闻失败:', error);
    ElMessage.error('获取网站新闻失败');
  } finally {
    websiteNewsDialog.loading = false;
  }
};

const fetchWebsiteNews = async () => {
  try {
    websiteNewsDialog.loading = true;
    
    const params = {
      sourceUrl: websiteNewsDialog.sourceUrl,
      taskId: taskDetailDialog.taskId,
      page: websiteNewsDialog.pagination.current,
      size: websiteNewsDialog.pagination.size
    };
    
    const result = await taskLogApi.getWebsiteNews(params);
    websiteNewsDialog.newsData = result.records || [];
    websiteNewsDialog.pagination.total = result.total || 0;
    
  } catch (error) {
    console.error('获取网站新闻失败:', error);
    ElMessage.error('获取网站新闻失败');
  } finally {
    websiteNewsDialog.loading = false;
  }
};

const handleWebsiteNewsClose = () => {
  websiteNewsDialog.visible = false;
  websiteNewsDialog.sourceUrl = '';
  websiteNewsDialog.newsData = [];
  websiteNewsDialog.pagination.current = 1;
  websiteNewsDialog.pagination.total = 0;
};

const handleWebsiteNewsPageChange = (page) => {
  websiteNewsDialog.pagination.current = page;
  fetchWebsiteNews();
};

const handleWebsiteNewsSizeChange = (size) => {
  websiteNewsDialog.pagination.size = size;
  websiteNewsDialog.pagination.current = 1;
  fetchWebsiteNews();
};

const getStatusTagType = (status) => {
  if (!status) return 'info';
  switch (status.toLowerCase()) {
    case 'success':
    case 'completed':
    case 'published':
      return 'success';
    case 'pending':
    case 'processing':
      return 'warning';
    case 'failed':
    case 'error':
      return 'danger';
    default:
      return 'info';
  }
};

const getStatusText = (status) => {
  if (!status) return '未知';
  switch (status.toLowerCase()) {
    case 'success':
      return '成功';
    case 'completed':
      return '完成';
    case 'published':
      return '已发布';
    case 'pending':
      return '待处理';
    case 'processing':
      return '处理中';
    case 'failed':
      return '失败';
    case 'error':
      return '错误';
    default:
      return status;
  }
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  
  // 清理HTML标签（作为额外的保护，即使后端应该返回纯文本）
  let cleanText = text.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
  
  if (cleanText.length <= maxLength) return cleanText;
  return cleanText.substring(0, maxLength) + '...';
};

// 任务导出方法
const handleTaskExport = async (task, command) => {
  try {
    // 构建导出参数 - 按任务ID导出
    const exportParams = {
      exportFormat: command,
      taskId: task.id, // 关键：按任务ID导出
      onlyRelevant: true // 只导出相关新闻
    };
    
    ElMessage.info(`正在导出任务【${task.taskType || '未知任务'}】的相关新闻，请稍候...`);
    
    const response = await newsExportApi.exportNews(exportParams);
    
    // 处理文件下载
    const blob = new Blob([response], {
      type: command === 'pdf' ? 'application/pdf' : 'application/json'
    });
    
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // 生成文件名：任务类型_任务ID_时间戳
    const timestamp = new Date().getTime();
    const taskType = task.taskType ? task.taskType.replace(/\s+/g, '_') : 'task';
    const fileName = `${taskType}_${task.id}_${timestamp}.${command}`;
    
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('导出成功，文件已开始下载');
  } catch (error) {
    console.error('任务导出失败:', error);
    
    // 尝试从错误响应中获取错误信息
    let errorMessage = '导出失败';
    if (error.response && error.response.data) {
      try {
        // 对于blob响应，需要先读取
        if (error.response.data instanceof Blob) {
          const errorText = await error.response.data.text();
          try {
            const errorObj = JSON.parse(errorText);
            errorMessage = errorObj.message || errorObj.msg || errorMessage;
          } catch (e) {
            errorMessage = errorText || errorMessage;
          }
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        }
      } catch (e) {
        console.error('解析错误信息失败:', e);
      }
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    ElMessage.error(`导出失败: ${errorMessage}`);
  }
};

// 高级导出对话框
const exportDialog = reactive({
  visible: false,
  loading: false,
  formData: {
    exportFormat: 'pdf',
    onlyRelevant: true,
    startTime: '',
    endTime: '',
    status: '',
    keyword: '',
    sourceType: ''
  }
});

const handleAdvancedExport = () => {
  // 初始化表单数据
  exportDialog.formData.startTime = searchParams.startDate || '';
  exportDialog.formData.endTime = searchParams.endDate || '';
  exportDialog.visible = true;
};

const handleExportSubmit = async () => {
  try {
    exportDialog.loading = true;
    
    // 构建导出参数
    const exportParams = {
      ...exportDialog.formData,
      startTime: exportDialog.formData.startTime || null,
      endTime: exportDialog.formData.endTime || null
    };
    
    ElMessage.info('正在生成导出文件，请稍候...');
    
    const response = await newsExportApi.exportNews(exportParams);
    
    // 处理文件下载（同上）
    const blob = new Blob([response], {
      type: exportDialog.formData.exportFormat === 'pdf' ? 'application/pdf' : 'application/json'
    });
    
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `news_export_${new Date().getTime()}.${exportDialog.formData.exportFormat}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    exportDialog.visible = false;
    ElMessage.success('导出成功，文件已开始下载');
    
  } catch (error) {
    console.error('高级导出失败:', error);
    ElMessage.error('导出失败: ' + (error.message || '未知错误'));
  } finally {
    exportDialog.loading = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};
</script>

<style scoped>
.task-log-list {
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
.search-form .el-select,
.search-form .el-date-picker {
  width: 100%;
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

.website-summary-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.url-link {
  word-break: break-all;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.no-link {
  color: #c0c4cc;
  font-size: 12px;
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
</style>