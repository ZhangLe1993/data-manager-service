import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from '../views/Index.vue'

Vue.use(VueRouter);

const routes = [
    {
        path: '/**',
        name: 'Index',
        component: Index,
        meta:  {title: '数据库管理平台'},
    },
    {
        path: '/index',
        name: 'Index',
        component: Index,
        meta:  {title: '数据库管理平台'},
    },
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
});

const originalPush = VueRouter.prototype.push
  VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

export default router
