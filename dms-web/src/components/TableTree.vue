<template>
  <div>
    <div :class="['box', theme]" ref="box" style="height: calc(100vh - 145px); overflow: auto">
      <el-input
          placeholder="输入关键字进行过滤"
          v-model="filterText">
      </el-input>
      <el-tree
          v-loading="loading"
          element-loading-text="拼命加载中"
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(0, 0, 0, 0.8)"
          class="filter-tree"
          :data="data"
          :props="defaultProps"
          :default-expanded-keys="defaultExpandedKeys"
          :load="loadNode"
          lazy
          ref="tree"
          :filter-node-method="filterNode"
          :highlight-current="true"
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
      <!--编辑和删除Node-->
      <v-contextmenu-item v-if="isTableClick" >新查询</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >设计表</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >重命名</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >删除表</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >清空表</v-contextmenu-item>
    </v-contextmenu>
  </div>
</template>

<script>

export default {
  name: "TableTree",
  props: {
    addEditTab: Function,
    theme: String,
    schemaItem: Object,
  },
  components: {

  },
  data() {
    return {
      filterText: '',
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
      isTableClick: false,
      isFieldClick: false,
      currentClickNodeData: null,
    };
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  async mounted() {
    this.loading = true;
    await this.renderTable();
    this.loading = false;
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    async loadNode(node, resolve) {
      const data = node.data;
      switch(data.type) {
        case 'table':
          // eslint-disable-next-line no-case-declarations
          const fieldInfo = await this.getFields(data.schemaId, data.schema, data.name);
          // eslint-disable-next-line no-case-declarations
          const fields = fieldInfo.columns;
          // eslint-disable-next-line no-unused-vars,no-case-declarations
          const target = [];
          for(var i = 0; i < fields.length; i++) {
            const obj = fields[i];
            if(fieldInfo.primaryKeys.includes(obj.name)) {
              target.push({schemaId: data.schemaId, icon: 'iconfont icon-yuechi', type: 'field', schema: data.schema, name: obj.name, leaf: true });
            } else {
              let icon = "iconfont icon-xitongmorenziduan";
              if(obj['type'].indexOf('INT') !== -1 || obj['type'].indexOf('NUMBER') !== -1) {
                icon = "iconfont icon-int"
              }
              if(obj['type'].indexOf('VARCHAR') !== -1) {
                icon = "iconfont icon-field"
              }
              if(obj['type'].indexOf('TEXT') !== -1) {
                icon = "iconfont icon-wenben"
              }
              if(obj['type'].indexOf('TIMESTAMP') !== -1) {
                icon = "iconfont icon-riqi"
              }
              if(obj['type'].indexOf('BOOLEAN') !== -1) {
                icon = "iconfont icon-a-Group16"
              }
              target.push({schemaId: data.schemaId, icon: icon, type: 'field', schema: data.schema, name: obj.name, leaf: true });
            }
          }
          return resolve(target);
        default:
          return resolve([]);
      }
    },
    async renderTable() {
      const res = await this.getTables(this.schemaItem.id, this.schemaItem.schema);
      const tableAndViewList = res.tables;
      let target = [];
      for(var i = 0; i < tableAndViewList.length; i++ ) {
        const obj = tableAndViewList[i];
        if(obj.type === 'TABLE') {
          target.push({schemaId: this.schemaItem.id, icon: 'iconfont icon-biao',type: 'table', schema: this.schemaItem.schema, name: obj.name, children: []});
        }
      }
      this.data = target;
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
      // console.log(res);
      if(res.status === 200) {
        return res.data;
      } else {
        return [];
      }
    },
    // eslint-disable-next-line no-unused-vars
    handleContextMenu(event, data, node) {
      // console.log(data);
      const postition = {
        top: event.clientY,
        left: event.clientX,
      }
      this.isItemClick = true;
      if(data.type === 'table') {
        this.isTableClick = true;
        this.isFieldClick = false;
      } else {
        this.isTableClick = false;
        this.isFieldClick = true;
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

    },

  },
}
</script>

<style>
.v-contextmenu--default .v-contextmenu-item--hover {
  background-color: #2c3e50 !important;
}
.v-contextmenu {
  padding-left: 10px !important;
  padding-right:10px !important;
}

.el-loading-mask {
  height: calc(100vh - 145px)
}

/*定义滚动条高宽及背景 高宽分别对应横竖滚动条的尺寸*/
::-webkit-scrollbar
{
  width: 5px;
  height: 5px;
  background-color: #F5F5F5;
}

/*定义滚动条轨道 内阴影+圆角*/
::-webkit-scrollbar-track
{
  -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.3);
  border-radius: 10px;
  background-color: #F5F5F5;
}

/*定义滑块 内阴影+圆角*/
::-webkit-scrollbar-thumb
{
  border-radius: 10px;
  -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,.3);
  background-color: #555;
}
</style>