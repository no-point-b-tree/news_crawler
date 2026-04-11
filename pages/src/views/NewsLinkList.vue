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
                         filterable
                         style="width: 100%"
                       >
                         <el-option
                           v-for="lang in getAvailableLanguages"
                           :key="lang.code"
                           :label="`${lang.displayName} (${lang.code})`"
                           :value="lang.code"
                         />
                       </el-select>
                     </el-form-item>
                   </el-col>
                   <el-col :xs="24" :sm="12" :md="8" :lg="6">
                     <el-form-item label="CSS选择器">
                       <el-select
                         v-model="searchParams.cssFilter"
                         placeholder="筛选CSS选择器"
                         clearable
                         style="width: 100%"
                       >
                         <el-option label="未配置" value="no_css" />
                         <el-option label="有配置" value="has_css" />
                         <el-option label="有启用的" value="has_enabled" />
                         <el-option label="全部禁用" value="all_disabled" />
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
               <div class="action-header">
                 <div class="action-buttons-left">
                   <el-button type="primary" @click="handleAdd">新增</el-button>
                   <el-button type="danger" @click="handleBatchDelete" :disabled="selection.length === 0">
                     批量删除
                   </el-button>

                 </div>
               <div class="css-stats" v-if="tableData.length > 0">
                 <span class="stat-item" v-if="searchParams.cssFilter">
                   <el-tag type="info" size="small">
                     {{ getFilterLabel(searchParams.cssFilter) }}
                   </el-tag>
                 </span>
                 <span class="stat-item">
                   <span class="stat-label">当前页:</span>
                   <span class="stat-value">{{ tableData.length }}</span>
                 </span>
                 <span class="stat-item">
                   <span class="stat-label">已配置CSS:</span>
                   <span 
                     class="stat-value success clickable" 
                     @click="handleBatchViewWithCss"
                     :title="getWebsitesWithCssCount.value > 0 ? '点击查看已配置CSS的网站' : '没有已配置CSS的网站'"
                   >
                     {{ getWebsitesWithCssCount }}
                   </span>
                 </span>
                 <span class="stat-item">
                   <span class="stat-label">未配置CSS:</span>
                   <span 
                     class="stat-value danger clickable" 
                     @click="handleBatchViewNoCss"
                     :title="getWebsitesWithoutCssCount.value > 0 ? '点击查看未配置CSS的网站' : '没有未配置CSS的网站'"
                   >
                     {{ getWebsitesWithoutCssCount }}
                   </span>
                 </span>
                 <span class="stat-item">
                   <span class="stat-label">CSS总数:</span>
                   <span class="stat-value">{{ getTotalCssCount }}</span>
                 </span>
               </div>
               </div>
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
        <el-table-column prop="cssSelectorCount" label="CSS选择器" min-width="120" sortable>
          <template #header>
            <div style="display: flex; align-items: center; gap: 5px;">
              <span>CSS选择器</span>
              <el-tooltip content="点击数字可查看详情" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <template #default="{ row }">
            <div class="css-selector-count" @click="handleViewCssSelectors(row)">
              <el-tooltip 
                :content="`总数: ${row.cssSelectorCount || 0}, 启用: ${row.enabledCssSelectorCount || 0}, 禁用: ${row.disabledCssSelectorCount || 0}\n点击查看详情`" 
                placement="top"
              >
                <div class="count-container" style="cursor: pointer;">
                  <el-tag 
                    :type="getCssCountType(row)" 
                    size="small"
                    class="count-tag"
                  >
                    {{ row.cssSelectorCount || 0 }}
                  </el-tag>
                  <span class="count-details">
                    <span class="enabled-count" v-if="row.enabledCssSelectorCount > 0">
                      {{ row.enabledCssSelectorCount }}启
                    </span>
                    <span class="disabled-count" v-if="row.disabledCssSelectorCount > 0">
                      {{ row.disabledCssSelectorCount }}禁
                    </span>
                  </span>
                </div>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="140">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="success" size="small" @click="handleStartTask(row)">执行任务</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
              </el-table>

               <div class="pagination" v-if="pagination.total > 0">
                 <div class="pagination-info">
                   <span v-if="searchParams.cssFilter && tableData.length === 0" class="filter-empty-tip">
                     <el-icon><Warning /></el-icon>
                     当前筛选条件下无数据，请调整筛选条件
                   </span>
                   <span v-else-if="searchParams.cssFilter" class="filter-tip">
                     当前显示筛选结果 ({{ tableData.length }} 条)
                   </span>
                 </div>
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
            filterable
          >
            <el-option-group
              v-for="group in getLanguageGroups"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="lang in group.options"
                :key="lang.code"
                :label="`${lang.displayName} (${lang.code})`"
                :value="lang.code"
              />
            </el-option-group>
          </el-select>
          <div class="form-tip">使用Java Locale支持的语言代码</div>
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
      width="900px"
    >
      <div class="css-selector-dialog">
        <el-card class="add-css-selector-card">
          <template #header>
            <div class="card-header">
              <span>添加CSS选择器配置</span>
            </div>
          </template>
          <el-form
            ref="cssSelectorFormRef"
            :model="cssSelectorDialog.formData"
            :rules="cssSelectorDialog.formRules"
            label-width="140px"
            label-position="left"
          >
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="CSS选择器组" prop="cssSelectorGroup">
                  <el-input
                    v-model="cssSelectorDialog.formData.cssSelectorGroup"
                    placeholder="请输入CSS选择器组（如：.article-list .item）"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期选择器" prop="dateCssSelector">
                  <el-input
                    v-model="cssSelectorDialog.formData.dateCssSelector"
                    placeholder="请输入日期选择器（如：.date）"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超链接选择器" prop="hyperLinkCssSelector">
                  <el-input
                    v-model="cssSelectorDialog.formData.hyperLinkCssSelector"
                    placeholder="请输入超链接选择器（如：a.title）"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日期匹配规则" prop="datePattern">
                  <el-input
                    v-model="cssSelectorDialog.formData.datePattern"
                    placeholder="请输入日期匹配规则（如：yyyy-MM-dd HH:mm:ss）"
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="启用状态" prop="enabled">
                  <el-select
                    v-model="cssSelectorDialog.formData.enabled"
                    placeholder="请选择启用状态"
                    style="width: 100%"
                  >
                    <el-option label="启用" value="ON" />
                    <el-option label="关闭" value="OFF" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item>
                  <el-button type="primary" @click="handleAddCssSelector" :loading="cssSelectorDialog.loading">
                    添加配置
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>已配置的CSS选择器</span>
            </div>
          </template>
           <el-table :data="cssSelectorDialog.data" style="width: 100%">
            <el-table-column prop="cssSelectorGroup" label="CSS选择器组" min-width="150" />
            <el-table-column prop="dateCssSelector" label="日期选择器" min-width="120" />
            <el-table-column prop="hyperLinkCssSelector" label="超链接选择器" min-width="130" />
            <el-table-column prop="datePattern" label="日期匹配规则" min-width="150" />
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled === 'ON' ? 'success' : 'danger'" size="small">
                  {{ row.enabled === 'ON' ? '启用' : '关闭' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="140">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="handleEditCssSelector(row)">编辑</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteCssSelector(row)">删除</el-button>
                </div>
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
        </el-card>
      </div>
    </el-dialog>

    <el-dialog
      v-model="cssSelectorEditDialog.visible"
      :title="cssSelectorEditDialog.title"
      width="600px"
      @close="handleCssSelectorEditClose"
    >
      <el-form
        ref="cssSelectorEditFormRef"
        :model="cssSelectorEditDialog.formData"
        :rules="cssSelectorDialog.formRules"
        label-width="140px"
        label-position="left"
      >
        <el-form-item label="CSS选择器组" prop="cssSelectorGroup">
          <el-input
            v-model="cssSelectorEditDialog.formData.cssSelectorGroup"
            placeholder="请输入CSS选择器组"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="日期选择器" prop="dateCssSelector">
          <el-input
            v-model="cssSelectorEditDialog.formData.dateCssSelector"
            placeholder="请输入日期选择器"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="超链接选择器" prop="hyperLinkCssSelector">
          <el-input
            v-model="cssSelectorEditDialog.formData.hyperLinkCssSelector"
            placeholder="请输入超链接选择器"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="日期匹配规则" prop="datePattern">
          <el-input
            v-model="cssSelectorEditDialog.formData.datePattern"
            placeholder="请输入日期匹配规则（如：yyyy-MM-dd HH:mm:ss）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-select
            v-model="cssSelectorEditDialog.formData.enabled"
            placeholder="请选择启用状态"
            style="width: 100%"
          >
            <el-option label="启用" value="ON" />
            <el-option label="关闭" value="OFF" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cssSelectorEditDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleCssSelectorEditSubmit" :loading="cssSelectorEditDialog.loading">
            确定
          </el-button>
        </span>
       </template>
     </el-dialog>

     <el-dialog
       v-model="taskTimeDialog.visible"
       :title="taskTimeDialog.title"
       width="500px"
       @close="handleTaskTimeClose"
     >
       <el-form
         ref="taskTimeFormRef"
         :model="taskTimeDialog.formData"
         :rules="taskTimeDialog.formRules"
         label-width="120px"
         label-position="left"
       >
         <el-form-item label="开始时间" prop="startDate">
           <el-date-picker
             v-model="taskTimeDialog.formData.startDate"
             type="date"
             placeholder="选择开始日期"
             value-format="YYYY-MM-DD"
             style="width: 100%"
           />
         </el-form-item>
         <el-form-item label="结束时间" prop="endDate">
           <el-date-picker
             v-model="taskTimeDialog.formData.endDate"
             type="date"
             placeholder="选择结束日期"
             value-format="YYYY-MM-DD"
             style="width: 100%"
           />
         </el-form-item>
       </el-form>

       <template #footer>
         <span class="dialog-footer">
           <el-button @click="handleTaskTimeClose">取消</el-button>
           <el-button type="primary" @click="handleTaskTimeSubmit" :loading="taskTimeDialog.loading">
             开始执行
           </el-button>
         </span>
       </template>
     </el-dialog>
   </div>
 </template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { InfoFilled, Warning } from '@element-plus/icons-vue';
import { newsLinkApi } from '@/api';

const searchParams = reactive({
  keyword: '',
  langs: [],
  cssFilter: '' // 新增：CSS选择器筛选条件
});

const tableData = ref([]);
const loading = ref(false);
const selection = ref([]);
const languages = ref([]);
const localeLanguages = ref([]); // Java Locale支持的语言
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

const cssSelectorDialog = reactive({
  visible: false,
  url: '',
  loading: false,
  data: [],
  pagination: {
    current: 1,
    size: 10,
    total: 0
  },
  formData: {
    cssSelectorGroup: '',
    dateCssSelector: '',
    hyperLinkCssSelector: '',
    datePattern: '',
    enabled: 'ON'
  },
  formRules: {
    cssSelectorGroup: [
      { required: true, message: '请输入CSS选择器组', trigger: 'blur' },
      { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
    ],
    dateCssSelector: [
      { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' }
    ],
    hyperLinkCssSelector: [
      { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' }
    ],
    datePattern: [
      { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
    ],
    enabled: [
      { required: true, message: '请选择启用状态', trigger: 'change' }
    ]
  }
});

const cssSelectorEditDialog = reactive({
  visible: false,
  title: '编辑CSS选择器配置',
  loading: false,
  isEdit: false,
  currentId: null,
  formData: {
    cssSelectorGroup: '',
    dateCssSelector: '',
    hyperLinkCssSelector: '',
    datePattern: '',
    enabled: 'ON'
  }
});

const cssSelectorFormRef = ref();
const cssSelectorEditFormRef = ref();

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
  lang: ''
});

const taskTimeDialog = reactive({
  visible: false,
  title: '选择任务时间范围',
  loading: false,
  currentUrl: '',
  formData: {
    startDate: '',
    endDate: ''
  },
  formRules: {
    startDate: [
      { required: true, message: '请选择开始时间', trigger: 'change' }
    ],
    endDate: [
      { required: true, message: '请选择结束时间', trigger: 'change' }
    ]
  }
});

const taskTimeFormRef = ref();

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
    { required: true, message: '请选择语言', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择语言'));
        } else if (value.length > 10) {
          callback(new Error('语言代码长度不能超过10个字符'));
        } else {
          // 验证是否是有效的语言代码
          const isValid = getAvailableLanguages.value.some(lang => lang.code === value);
          if (!isValid) {
            callback(new Error('请选择有效的语言代码'));
          } else {
            callback();
          }
        }
      },
      trigger: 'change'
    }
  ]
};

const formRef = ref();

onMounted(() => {
  fetchLanguages();
  fetchLocaleLanguages();
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

const fetchLocaleLanguages = async () => {
  try {
    const res = await newsLinkApi.getLocaleLanguages();
    localeLanguages.value = res;
  } catch (error) {
    console.error('获取Locale语言列表失败:', error);
    // 如果获取失败，使用默认的常用语言
    localeLanguages.value = [
      { code: 'zh', displayName: 'Chinese' },
      { code: 'en', displayName: 'English' },
      { code: 'ja', displayName: 'Japanese' },
      { code: 'ko', displayName: 'Korean' },
      { code: 'fr', displayName: 'French' },
      { code: 'de', displayName: 'German' },
      { code: 'es', displayName: 'Spanish' },
      { code: 'ru', displayName: 'Russian' }
    ];
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    // 先获取所有数据（因为后端不支持CSS筛选）
    const params = {
      keyword: searchParams.keyword,
      langs: searchParams.langs,
      page: pagination.current,
      size: pagination.size
    };
    console.log('请求参数:', params);
    
    // 使用新的接口获取包含CSS选择器数量的数据
    const res = await newsLinkApi.pageNewsLinksWithCssCount(params);
    console.log('API响应:', res);
    
    let filteredRecords = res.records || [];
    
    // 客户端筛选CSS选择器
    if (searchParams.cssFilter) {
      filteredRecords = filteredRecords.filter(item => {
        switch (searchParams.cssFilter) {
          case 'no_css':
            return !item.cssSelectorCount || item.cssSelectorCount === 0;
          case 'has_css':
            return item.cssSelectorCount && item.cssSelectorCount > 0;
          case 'has_enabled':
            return item.enabledCssSelectorCount && item.enabledCssSelectorCount > 0;
          case 'all_disabled':
            return item.cssSelectorCount && item.cssSelectorCount > 0 && 
                   (!item.enabledCssSelectorCount || item.enabledCssSelectorCount === 0);
          default:
            return true;
        }
      });
    }
    
    tableData.value = filteredRecords;
    // 保持后端返回的总数，不修改为筛选后的数量
    pagination.total = res.total || 0;
    
    console.log('分页数据:', { 
      records: tableData.value.length, 
      total: pagination.total,
      filtered: filteredRecords.length 
    });
  } catch (error) {
    console.error('获取数据失败:', error);
    // 如果新接口失败，回退到旧接口
    try {
      const params = {
        keyword: searchParams.keyword,
        langs: searchParams.langs,
        page: pagination.current,
        size: pagination.size
      };
      const res = await newsLinkApi.pageNewsLinks(params);
      let filteredRecords = res.records || [];
      
      // 为旧接口数据添加默认的CSS选择器数量
      filteredRecords.forEach(item => {
        item.cssSelectorCount = 0;
        item.enabledCssSelectorCount = 0;
        item.disabledCssSelectorCount = 0;
      });
      
      // 客户端筛选CSS选择器
      if (searchParams.cssFilter) {
        filteredRecords = filteredRecords.filter(item => {
          switch (searchParams.cssFilter) {
            case 'no_css':
              return true; // 旧接口数据都没有CSS配置
            case 'has_css':
              return false; // 旧接口数据都没有CSS配置
            case 'has_enabled':
              return false; // 旧接口数据都没有启用的CSS
            case 'all_disabled':
              return false; // 旧接口数据都没有CSS配置
            default:
              return true;
          }
        });
      }
      
      tableData.value = filteredRecords;
      pagination.total = res.total || 0;
    } catch (fallbackError) {
      console.error('回退接口也失败:', fallbackError);
    }
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
  searchParams.cssFilter = '';
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

// 监听CSS筛选条件变化，重置到第一页
watch(() => searchParams.cssFilter, () => {
  pagination.current = 1;
});

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
    lang: ''
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
    lang: row.lang || ''
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

const handleStartTask = (row) => {
  // 设置默认时间为前一天到今天
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(startDate.getDate() - 1);
  
  // 格式化日期为 YYYY-MM-DD
  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };
  
  taskTimeDialog.currentUrl = row.url;
  taskTimeDialog.formData.startDate = formatDate(startDate);
  taskTimeDialog.formData.endDate = formatDate(endDate);
  taskTimeDialog.visible = true;
  
  if (taskTimeFormRef.value) {
    taskTimeFormRef.value.clearValidate();
  }
};

const handleTaskTimeSubmit = async () => {
  try {
    await taskTimeFormRef.value.validate();
    
    taskTimeDialog.loading = true;
    
    // 调用API启动任务，传递时间范围参数
    await newsLinkApi.startSingleTask(taskTimeDialog.currentUrl, {
      startDate: taskTimeDialog.formData.startDate,
      endDate: taskTimeDialog.formData.endDate
    });
    
    ElMessage.success('任务已提交，正在后台执行');
    
    // 关闭对话框
    taskTimeDialog.visible = false;
    
    // 重置表单
    taskTimeDialog.formData.startDate = '';
    taskTimeDialog.formData.endDate = '';
    
  } catch (error) {
    if (error && error.errors) {
      ElMessage.error('请检查表单数据');
    } else if (error !== 'cancel') {
      ElMessage.error('启动任务失败');
    }
  } finally {
    taskTimeDialog.loading = false;
  }
};

const handleTaskTimeClose = () => {
  taskTimeDialog.visible = false;
  taskTimeDialog.formData.startDate = '';
  taskTimeDialog.formData.endDate = '';
  taskTimeDialog.currentUrl = '';
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

const handleBatchViewNoCss = () => {
  const noCssWebsites = tableData.value.filter(item => !item.cssSelectorCount || item.cssSelectorCount === 0);
  if (noCssWebsites.length === 0) {
    ElMessage.info('没有未配置CSS选择器的网站');
    return;
  }
  
  ElMessageBox.confirm(
    `发现 ${noCssWebsites.length} 个网站未配置CSS选择器，是否批量查看？`,
    '批量查看未配置CSS',
    {
      confirmButtonText: '查看',
      cancelButtonText: '取消',
      type: 'warning',
      showClose: false
    }
  ).then(() => {
    // 可以在这里实现批量查看逻辑，比如打开多个标签页
    // 这里先简单提示
    ElMessage.info(`共 ${noCssWebsites.length} 个网站需要配置CSS选择器`);
    
    // 可以逐个打开
    noCssWebsites.forEach((website, index) => {
      setTimeout(() => {
        handleViewCssSelectors(website);
      }, index * 500); // 间隔500ms打开，避免同时打开太多
    });
  }).catch(() => {
    // 用户取消
  });
};

const handleBatchViewWithCss = () => {
  const withCssWebsites = tableData.value.filter(item => item.cssSelectorCount && item.cssSelectorCount > 0);
  if (withCssWebsites.length === 0) {
    ElMessage.info('没有已配置CSS选择器的网站');
    return;
  }
  
  ElMessageBox.confirm(
    `发现 ${withCssWebsites.length} 个网站已配置CSS选择器，是否批量查看？`,
    '批量查看已配置CSS',
    {
      confirmButtonText: '查看',
      cancelButtonText: '取消',
      type: 'info',
      showClose: false
    }
  ).then(() => {
    ElMessage.info(`共 ${withCssWebsites.length} 个网站已配置CSS选择器`);
    
    // 逐个打开
    withCssWebsites.forEach((website, index) => {
      setTimeout(() => {
        handleViewCssSelectors(website);
      }, index * 500);
    });
  }).catch(() => {
    // 用户取消
  });
};

const handleViewCssSelectors = async (row) => {
  cssSelectorDialog.visible = true;
  cssSelectorDialog.url = row.url;
  cssSelectorDialog.pagination.current = 1;
  
  Object.assign(cssSelectorDialog.formData, {
    cssSelectorGroup: '',
    dateCssSelector: '',
    hyperLinkCssSelector: '',
    enabled: 'ON'
  });
  
  if (cssSelectorFormRef.value) {
    cssSelectorFormRef.value.clearValidate();
  }
  
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
  try {
    await cssSelectorFormRef.value.validate();
    
    cssSelectorDialog.loading = true;
    
    const formData = {
      cssSelectorGroup: cssSelectorDialog.formData.cssSelectorGroup,
      dateCssSelector: cssSelectorDialog.formData.dateCssSelector,
      hyperLinkCssSelector: cssSelectorDialog.formData.hyperLinkCssSelector,
      datePattern: cssSelectorDialog.formData.datePattern,
      enabled: cssSelectorDialog.formData.enabled
    };
    
    await newsLinkApi.addCssSelector(cssSelectorDialog.url, formData);
    ElMessage.success('添加成功');
    
    Object.assign(cssSelectorDialog.formData, {
      cssSelectorGroup: '',
      dateCssSelector: '',
      hyperLinkCssSelector: '',
      datePattern: '',
      enabled: 'ON'
    });
    
    if (cssSelectorFormRef.value) {
      cssSelectorFormRef.value.clearValidate();
    }
    
    fetchCssSelectors();
  } catch (error) {
    if (error && error.errors) {
      ElMessage.error('请检查表单数据');
    } else {
      ElMessage.error('添加失败');
    }
  } finally {
    cssSelectorDialog.loading = false;
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

const handleEditCssSelector = (row) => {
  cssSelectorEditDialog.visible = true;
  cssSelectorEditDialog.title = '编辑CSS选择器配置';
  cssSelectorEditDialog.isEdit = true;
  cssSelectorEditDialog.currentId = row.id;
  
  Object.assign(cssSelectorEditDialog.formData, {
    cssSelectorGroup: row.cssSelectorGroup || '',
    dateCssSelector: row.dateCssSelector || '',
    hyperLinkCssSelector: row.hyperLinkCssSelector || '',
    datePattern: row.datePattern || '',
    enabled: row.enabled || 'ON'
  });
  
  if (cssSelectorEditFormRef.value) {
    cssSelectorEditFormRef.value.clearValidate();
  }
};

const handleCssSelectorEditClose = () => {
  cssSelectorEditDialog.loading = false;
};

const handleCssSelectorEditSubmit = async () => {
  try {
    await cssSelectorEditFormRef.value.validate();
    
    cssSelectorEditDialog.loading = true;
    
    const formData = {
      cssSelectorGroup: cssSelectorEditDialog.formData.cssSelectorGroup,
      dateCssSelector: cssSelectorEditDialog.formData.dateCssSelector,
      hyperLinkCssSelector: cssSelectorEditDialog.formData.hyperLinkCssSelector,
      datePattern: cssSelectorEditDialog.formData.datePattern,
      enabled: cssSelectorEditDialog.formData.enabled
    };
    
    await newsLinkApi.updateCssSelector(cssSelectorEditDialog.currentId, formData);
    ElMessage.success('更新成功');
    
    cssSelectorEditDialog.visible = false;
    fetchCssSelectors();
  } catch (error) {
    if (error && error.errors) {
      ElMessage.error('请检查表单数据');
    } else {
      ElMessage.error('更新失败');
    }
  } finally {
    cssSelectorEditDialog.loading = false;
  }
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

const getCssCountType = (row) => {
  if (!row.cssSelectorCount || row.cssSelectorCount === 0) {
    return 'danger'; // 红色：没有配置CSS选择器
  } else if (row.enabledCssSelectorCount === 0) {
    return 'warning'; // 黄色：有配置但都禁用了
  } else {
    return 'success'; // 绿色：有启用的CSS选择器
  }
};

// 获取筛选标签
const getFilterLabel = (filterValue) => {
  const filterMap = {
    'no_css': '未配置CSS',
    'has_css': '有CSS配置',
    'has_enabled': '有启用的CSS',
    'all_disabled': '全部禁用'
  };
  return filterMap[filterValue] || filterValue;
};

// 计算属性
const getWebsitesWithCssCount = computed(() => {
  return tableData.value.filter(item => item.cssSelectorCount && item.cssSelectorCount > 0).length;
});

const getWebsitesWithoutCssCount = computed(() => {
  return tableData.value.filter(item => !item.cssSelectorCount || item.cssSelectorCount === 0).length;
});

const getTotalCssCount = computed(() => {
  return tableData.value.reduce((total, item) => total + (item.cssSelectorCount || 0), 0);
});

// 获取可用的语言列表（合并数据库中的语言和Locale支持的语言）
const getAvailableLanguages = computed(() => {
  const allLanguages = new Map();
  
  // 添加Locale支持的语言
  localeLanguages.value.forEach(lang => {
    allLanguages.set(lang.code, {
      code: lang.code,
      displayName: lang.displayName,
      source: 'locale'
    });
  });
  
  // 添加数据库中已有的语言（如果不在Locale列表中）
  languages.value.forEach(code => {
    if (!allLanguages.has(code)) {
      allLanguages.set(code, {
        code: code,
        displayName: code,
        source: 'database'
      });
    }
  });
  
  // 转换为数组并按显示名称排序
  return Array.from(allLanguages.values())
    .sort((a, b) => a.displayName.localeCompare(b.displayName));
});

// 获取分组的语言列表（用于表单选择）
const getLanguageGroups = computed(() => {
  const groups = [
    {
      label: '常用语言',
      options: []
    },
    {
      label: '所有语言',
      options: []
    }
  ];
  
  // 定义常用语言
  const commonLanguageCodes = ['zh', 'en', 'ja', 'ko', 'fr', 'de', 'es', 'ru', 'ar', 'pt'];
  
  // 获取所有语言
  const allLanguages = getAvailableLanguages.value;
  
  // 分离常用语言和其他语言
  allLanguages.forEach(lang => {
    if (commonLanguageCodes.includes(lang.code)) {
      groups[0].options.push(lang);
    } else {
      groups[1].options.push(lang);
    }
  });
  
  // 如果常用语言组为空，只显示所有语言组
  if (groups[0].options.length === 0) {
    return [{
      label: '所有语言',
      options: allLanguages
    }];
  }
  
  return groups;
});
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

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.action-buttons-left {
  display: flex;
  gap: 10px;
}

.css-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
}

.stat-label {
  color: #666;
}

.stat-value {
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #f5f5f5;
}

.stat-value.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.stat-value.danger {
  background-color: #fef0f0;
  color: #f56c6c;
}

.stat-value.clickable {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-value.clickable:hover {
  opacity: 0.8;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
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
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  min-height: 60px;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-tip {
  font-size: 14px;
  color: #409eff;
  background-color: #ecf5ff;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #d9ecff;
}

.filter-empty-tip {
  font-size: 14px;
  color: #e6a23c;
  background-color: #fdf6ec;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #faecd8;
  display: flex;
  align-items: center;
  gap: 5px;
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

.css-selector-dialog .add-css-selector-card {
  margin-bottom: 20px;
}

.css-selector-dialog .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.css-selector-dialog .el-form-item {
  margin-bottom: 18px;
}

.css-selector-dialog .el-row {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .css-selector-dialog .el-col {
    margin-bottom: 10px;
  }
  
  .css-selector-dialog .el-form-item {
    margin-bottom: 15px;
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

.css-selector-count .count-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.css-selector-count .count-tag {
  min-width: 40px;
  text-align: center;
  font-weight: bold;
}

.css-selector-count .count-details {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #666;
  line-height: 1.2;
}

.css-selector-count .enabled-count {
  color: #67c23a;
}

.css-selector-count .disabled-count {
  color: #f56c6c;
}



/* 鼠标悬停提示样式 */
.el-tooltip__popper {
  max-width: 300px;
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