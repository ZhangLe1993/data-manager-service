import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import api from './service';
import contentmenu from 'v-contextmenu'
import 'v-contextmenu/dist/index.css'

/*
* cnpm install --save axios
* cnpm install --save element-ui
* -- cnpm install --save element-ui/lib/theme-chalk/index.css
* cnpm install --save vue-router
* 右键菜单
* cnpm i v-contextmenu -S
* cnpm i -S v-contextmenu
* cnpm install --save vue-runtime-helpers
* 编辑器
* cnpm install --save vue2-ace-editor
* 格式刷
* cnpm install --save sql-formatter@2.3.3
* 剪切板
* cnpm install --save clipboard

* */
Vue.prototype.$api = api;

Vue.config.productionTip = false

/* elementUI */
Vue.use(ElementUI);

Vue.use(contentmenu);

router.beforeEach((to, from, next) => {
  if(to.meta.title) {
    document.title = to.meta.title;
  }
  next();
});

new Vue({
  router,
  render: h => h(App)
}).$mount('#app');
