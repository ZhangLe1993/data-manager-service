<template>
  <div>
    <div :class="['box', theme]" ref="box" v-on:contextmenu.prevent="handleShow($event)" style="min-height: 785px">
      <el-tree
          class="filter-tree"
          :data="data"
          :props="defaultProps"
          :default-expanded-keys="defaultExpandedKeys"
          :load="loadNode"
          lazy
          ref="tree"
          :highlight-current="true"
          @node-click="handleNodeClick"
          @node-contextmenu="handleContextMenu"
      >
          <span class="custom-tree-node" slot-scope="{ node, data }">
            <span>
                <i :class="data.icon"> </i> {{ node.label }}
            </span>
          </span>
      </el-tree>
    </div>
    <v-contextmenu ref="contextmenu">
      <!-- 是否是点击空白区域 -->
      <v-contextmenu-item v-if="!isNodeClick" @click="handleAddConnect">新建连接</v-contextmenu-item>
      <!--编辑和删除Node-->
      <v-contextmenu-item v-if="isItemClick && currentClickNodeData.type === 'NODE'" @click="handleEditNode">编辑</v-contextmenu-item>
      <v-contextmenu-item v-if="isItemClick && currentClickNodeData.type === 'NODE'" @click="handleDeleteNode">删除</v-contextmenu-item>
    </v-contextmenu>

    <ConnectForm :dialogFormVisible="connectFormVisible" :cancel="handleConnectFormCancel" :form="connectForm" :treeRefresh="refresh" :currentClickNodeData="currentClickNodeData" :connectTitle="connectTitle"/>
  </div>

</template>

<script>
import ConnectForm from '../components/ConnectForm';
export default {
  name: "Tree",
  props: {
    addTab: Function,
    theme: String,
  },
  components: {
    ConnectForm : ConnectForm,
  },
  data() {
    return {
      defaultExpandedKeys: [1],
      data: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf',
        icon: 'icon',
      },
      loading: true,
      connectFormVisible: false,
      connectForm: {
        id: 0,
        name: '',
        description: '',
        host: '',
        port : '',
        username : '',
        password : '',
      },
      isItemClick: false,
      isNodeClick: false,
      currentClickNodeData: null,
      connectTitle: '新增连接',
    };
  },
  methods: {
    async loadNode(node, resolve) {
      const data = node.data;
      switch(data.type) {
        case 'connection':
          // eslint-disable-next-line no-case-declarations
          const schemas = await this.schema(node);
          return resolve(schemas);
        case 'schema':
          console.log(data);
          // eslint-disable-next-line no-case-declarations
          const arr = [{connectionId: data.connectionId, icon: 'iconfont', type: 'tables-folder', schema: data.name, name: 'Tables', children: []}, {connectionId: data.connectionId, type: 'views-folder', schema: data.name, name: 'Views', children: []}, {connectionId: data.connectionId, type: 'procedures-folder', schema: data.name, name: 'Stored Procedures', children: []}, {connectionId: data.connectionId, type: 'functions-folder', schema: data.name, name: 'Functions', children: []},]
          return resolve(arr);
        case 'tables-folder':
          // eslint-disable-next-line no-case-declarations
          const tbs = await this.tableAndView(node, 'TABLE');
          return resolve(tbs);
        case 'views-folder':
          // eslint-disable-next-line no-case-declarations
          const views = await this.tableAndView(node, 'VIEW');
          return resolve(views);
        case 'table':
          // eslint-disable-next-line no-case-declarations
          const fieldInfo = await this.getFields(data.connectionId, data.schema, data.name);
          // eslint-disable-next-line no-case-declarations
          const fields = fieldInfo.columns;
          // eslint-disable-next-line no-unused-vars,no-case-declarations
          const target = [];
          for(var i = 0; i < fields.length; i++) {
            const obj = fields[i];
            if(fieldInfo.primaryKeys.includes(obj.name)) {
              target.push({connectionId: data.connectionId, icon: 'iconfont iconkey', type: 'field', schema: data.schema, name: obj.name, leaf: true });
            } else {
              target.push({connectionId: data.connectionId, icon: 'el-icon-coordinate', type: 'field', schema: data.schema, name: obj.name, leaf: true });
            }
          }
          return resolve(target);
        default:
          return resolve([]);
      }
    },
    getConnectionList() {
      const that = this;
      that.$api.get('/connection', { }, (res) => {
        this.data = res.data;
      });
    },
    async getSchema(id) {
      const that = this;
      const res = await that.$api.syncGet('/connection/schema', { id : id});
      if(res.status === 200) {
        return res.data;
      } else {
        return [];
      }
    },
    async schema(connectionNode) {
      const data = connectionNode.data;
      // eslint-disable-next-line no-case-declarations
      const res = await this.getSchema(data.id);
      // eslint-disable-next-line no-case-declarations
      const schemaList = res;
      // eslint-disable-next-line no-case-declarations
      let target = [];
      for(var i = 0; i < schemaList.length; i++ ) {
        const name = schemaList[i];
        target.push({connectionId: data.id, schema: name, icon: 'iconfont icondatabase' , type: 'schema', name: name, children: []});
      }
      return target;
    },
    async tableAndView(node, type) {
      const data = node.data;
      // eslint-disable-next-line no-case-declarations
      const res = await this.getTables(data.connectionId, data.schema);
      console.log(res);
      // eslint-disable-next-line no-case-declarations
      const tableList = res.tables;
      // eslint-disable-next-line no-case-declarations
      let target = [];
      for(var i = 0; i < tableList.length; i++ ) {
        const obj = tableList[i];
        if(obj.type === type) {
          target.push({connectionId: data.connectionId, icon: 'iconfont icontable',type: 'table', schema: data.schema, name: obj.name, children: []});
        }
      }
      return target;
    },
    async getTables(id, schema) {
      const that = this;
      const res = await that.$api.syncGet('/connection/schema/tables', { id : id, schema: schema});
      if(res.status === 200) {
        return res.data;
      } else {
        return [];
      }
    },
    async getFields(id, schema, table) {
      const that = this;
      const res = await that.$api.syncGet('/connection/schema/table/fields', { id : id, schema: schema, table : table});
      console.log(res);
      if(res.status === 200) {
        return res.data;
      } else {
        return [];
      }
    },
    // eslint-disable-next-line no-unused-vars
    handleNodeClick(node) {
      if(node.type === 'NODE') {
        console.log(node);
        this.addTab(node);
      }
    },
    // eslint-disable-next-line no-unused-vars
    handleContextMenu(event, data, node) {
      const postition = {
        top: event.clientY,
        left: event.clientX,
      }
      this.isItemClick = true;
      if(data.type === 'FOLDER') {
        this.isNodeClick = false;
      } else {
        this.isNodeClick = true;
      }
      this.currentClickNodeData = data;
      this.$refs.contextmenu.show(postition);
    },
    handleShow(event) {
      // var DOM = event.currentTarget;
      // 获取节点距离浏览器视口的高度
      var top = event.clientY;
      // 获取节点距离浏览器视口的宽度
      var left = event.clientX;
      const postition = {
        top: top,
        left: left,
      }
      this.isItemClick = false;
      this.$refs.contextmenu.show(postition);
      this.isNodeClick = false;
      this.currentClickNodeData = null;
    },
    handleHide() {
      this.$refs.contextmenu.hide();
    },
    refresh() {
      this.search();
    },
    search() {
      this.loading = true;
      const that = this;
      that.$api.get('/connection', { }, (res) => {
        this.data = res.data;
      });
    },
    handleAddConnect() {
      this.connectTitle = '新增连接';
      // form 重置
      this.connectForm = {id: 0, name: '', host : '', port : '', username : '', password : '' };
      this.connectFormVisible = true;
    },
    handleEditNode() {
      this.connectTitle = '编辑连接';
      this.connectForm = {id : this.currentClickNodeData.id, name: this.currentClickNodeData.name, host : this.currentClickNodeData.config.host, port : this.currentClickNodeData.config.port, username : this.currentClickNodeData.config.username, password : this.currentClickNodeData.config.password, };
      this.connectFormVisible = true;
    },
    handleDeleteNode() {
      console.log('删除链接');
      // 删除
      const that = this;
      that.$api.delete('/connection', {id : this.currentClickNodeData.id}, (res) => {
        if(res !== undefined && res.status !== undefined && res.status === 200) {
          this.openLayer('消息', '恭喜你，删除成功。', 'success');
          // 关闭弹出层
          this.refresh();
        } else {
          this.openLayer('消息', res.data, 'error');
        }
      });
    },
    handleConnectFormCancel() {
      this.connectFormVisible = false;
      this.connectForm = {id: 0, name: '', description: '', host : '', port : '', username : '', password : '' };
    },
    openLayer(title, msg, type) {
      if(type === 'error') {
        this.$notify.error({
          title: title,
          message: msg
        });
      } else {
        this.$notify({
          title: title,
          message: msg,
          type: type
        });
      }
    },
  },
  mounted() {
    this.getConnectionList();
  }
}
</script>

<style scoped>

</style>