import { createRouter, createWebHistory } from 'vue-router';
import Layout from '@/components/Layout.vue';

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/news-link',
    children: [
      {
        path: 'news-link',
        name: 'NewsLink',
        component: () => import('@/views/NewsLinkList.vue'),
        meta: { title: '新闻链接管理' }
      },
      {
        path: 'rss-link',
        name: 'RssLink',
        component: () => import('@/views/RssLinkList.vue'),
        meta: { title: 'RSS链接管理' }
      },
      {
        path: 'task-log',
        name: 'TaskLog',
        component: () => import('@/views/TaskLogList.vue'),
        meta: { title: '任务日志管理' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;