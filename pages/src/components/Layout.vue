<template>
  <div class="layout">
    <el-container>
      <el-header>
        <div class="header">
          <div class="logo">新闻爬虫管理系统</div>
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/news-link">新闻链接管理</el-menu-item>
            <el-menu-item index="/rss-link">RSS链接管理</el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => route.path);

const handleMenuSelect = (index) => {
  router.push(index);
};
</script>

<style scoped>
.layout {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  border-bottom: 1px solid #e6e6e6;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.el-menu {
  border-bottom: none;
}
</style>