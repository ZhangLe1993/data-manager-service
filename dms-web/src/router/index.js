import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from '../views/Index.vue'
import Home from '../views/Home'
import EditTableComponent from "@/components/EditTableComponent";

Vue.use(VueRouter);

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        meta:  {title: '数据库管理平台'},
    },
    {
        path: '/editTable',
        name: 'EditTableComponent',
        component: EditTableComponent,
        meta:  {title: '可编辑表格示例'},
    },
    {
        path: '/index',
        name: 'Index',
        component: Index,
        meta:  {title: '数据库管理平台'},
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
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
