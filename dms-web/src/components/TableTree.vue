<template>
  <div>
    <div :class="['box', theme]" ref="box" v-on:contextmenu.prevent="handleShow($event)" style="height: calc(100vh - 145px); overflow: auto">
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
          :data="treeData"
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
      <v-contextmenu-item @click="refreshTree">刷新</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" @click="addEditTab(currentRightClickNodeData)">新查询</v-contextmenu-item>
      <v-contextmenu-item v-if="currentRightClickNodeData === null" @click="openDesignTableDialog(false)">新建表</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" @click="openDesignTableDialog(true)">设计表</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" @click="openRenameTableDrawer">重命名</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >删除表</v-contextmenu-item>
      <v-contextmenu-item v-if="isTableClick" >清空表</v-contextmenu-item>
    </v-contextmenu>

    <!--  -->
    <RenameTableDrawer :dialog="renameDialogVisible" :loading="renameDialogSubmitLoading" :cancelForm="cancelRenameTableDrawerForm" :form="renameDialogForm" :currentRightClickNodeData="currentRightClickNodeData" :changeLoading="changeLoading" :refreshTree="refreshTree"></RenameTableDrawer>

    <DesignTable v-if="designTableVisible" :designTableVisible="designTableVisible" :closeDialog="closeDesignTableDialog" :tableInfo="currentRightClickNodeData" :editOpt="editOpt" :refreshTree="refreshTree" :schemaItem="schemaItem"></DesignTable>
  </div>
</template>

<script>
import RenameTableDrawer from "@/components/RenameTableDrawer";
import DesignTable from "@/components/DesignTable";
export default {
  name: "TableTree",
  props: {
    addEditTab: Function,
    theme: String,
    schemaItem: Object,
  },
  components: {
    RenameTableDrawer: RenameTableDrawer,
    DesignTable: DesignTable,
  },
  data() {
    return {
      editOpt: false,
      renameDialogVisible: false,
      renameDialogSubmitLoading: false,
      renameDialogForm: {
        name: '',
      },
      designTableVisible: false,
      filterText: '',
      defaultExpandedKeys: [1],
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: 'leaf',
        icon: 'icon',
      },
      loading: true,
      isTableClick: false,
      isFieldClick: false,
      currentRightClickNodeData: null,
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
            let icon = "iconfont icon-xitongmorenziduan";
            if(fieldInfo.primaryKeys.includes(obj.name)) {
              icon = "iconfont icon-yuechi"
            } else {
              if(obj['type'].indexOf('INT') !== -1 || obj['type'].indexOf('NUMBER') !== -1) {
                icon = "iconfont icon-int"
              }
              if(obj['type'].indexOf('VARCHAR') !== -1) {
                icon = "iconfont icon-field"
              }
              if(obj['type'].indexOf('TEXT') !== -1) {
                icon = "iconfont icon-wenben"
              }
              if(obj['type'].indexOf('TIME') !== -1 || obj['type'].indexOf('DATE') !== -1) {
                icon = "iconfont icon-riqi"
              }
              if(obj['type'].indexOf('BOOLEAN') !== -1) {
                icon = "iconfont icon-a-Group16"
              }
            }
            const callNull = obj.nullAble === 'YES';
            target.push({ schemaId: data.schemaId, icon: icon, type: 'field', schema: data.schema, name: obj.name, comment: obj.comment, callNull: callNull, len: obj.size, leaf: true  });
          }
          return resolve(target);
        default:
          return resolve([]);
      }
    },
    async refreshTree() {
      this.loading = true;
      await this.renderTable();
      this.loading = false;
    },
    async renderTable() {
      const res = await this.getTables(this.schemaItem.id, this.schemaItem.schema);
      const tableAndViewList = res.tables;
      let target = [];
      for(var i = 0; i < tableAndViewList.length; i++ ) {
        const obj = tableAndViewList[i];
        if(obj.type === 'TABLE') {
          target.push({
            schemaId: this.schemaItem.id,
            icon: 'iconfont icon-biao',
            type: 'table',
            schema: this.schemaItem.schema,
            name: obj.name,
            charset: obj.charset,
            comment: obj.comment,
            engine: obj.engine,
            autoIncrementNum: obj.autoIncrementNum,
            children: []
          });
        }
      }
      this.treeData = target;
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
      // console.log(this.schemaItem);
      const postition = {
        top: event.clientY + 20,
        left: event.clientX + 20,
      }
      if(data.type === 'table') {
        this.isTableClick = true;
        this.isFieldClick = false;
        this.currentRightClickNodeData = data;
        this.$refs.contextmenu.show(postition);
      }
    },
    handleShow(event) {
      // console.log(this.schemaItem);
      // var DOM = event.currentTarget;
      // 获取节点距离浏览器视口的高度
      var top = event.clientY;
      // 获取节点距离浏览器视口的宽度
      var left = event.clientX;
      const postition = {
        top: top + 20,
        left: left + 20,
      }
      this.isTableClick = false;
      this.isFieldClick = false;
      this.currentRightClickNodeData = null;
      this.$refs.contextmenu.show(postition);
    },
    handleHide() {
      this.$refs.contextmenu.hide();
    },
    openRenameTableDrawer() {
      this.renameDialogVisible = true;
      this.renameDialogForm.name = this.currentRightClickNodeData.name;
    },
    cancelRenameTableDrawerForm() {
      this.renameDialogVisible = false;
      this.renameDialogSubmitLoading = false;
    },
    changeLoading(loading) {
      this.renameDialogSubmitLoading = loading;
    },
    openDesignTableDialog(opt) {
      this.editOpt = opt;
      this.designTableVisible = true;
    },
    closeDesignTableDialog() {
      this.designTableVisible = false;
    }

  },
}
</script>

<style>
.v-contextmenu--default .v-contextmenu-item--hover {
  background-color: #2c3e50 !important;
}
.v-contextmenu {
  width: 100px !important;
  text-align: center !important;
  padding-left: 10px !important;
  padding-right:10px !important;
}
.v-contextmenu .v-contextmenu-item {
  line-height: 2 !important;
}

.el-loading-mask {
  height: calc(100vh - 145px)
}

.manage .el-tree-node {
  color: #000000;
}
</style>