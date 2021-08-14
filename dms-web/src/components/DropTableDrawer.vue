<template>
  <el-drawer
      title="删除表"
      :before-close="cancelForm"
      :visible.sync="dialog"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
  >
    <div class="demo-drawer__content">
      <el-form :model="form" ref="form">
        <el-form-item label="预览:" :label-width="formLabelWidth">
          <div class="code-container">
            <div class="code-text">
              &nbsp; | <span codex="sql"> drop table `{{ getOldName }}`;</span>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <div class="demo-drawer__footer">
        <el-button @click="cancelForm">取 消</el-button>
        <el-button type="primary" @click="handleSubmit()" :loading="loading">{{ loading ? '提交中 ...' : '确 定' }}</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
export default {
  name: "DropTableDrawer",
  props: {
    dialog: Boolean,
    loading: Boolean,
    cancelForm: Function,
    currentRightClickNodeData: Object,
    changeLoading: Function,
    refreshTree: Function,
  },
  computed: {
    getOldName() {
      if(this.currentRightClickNodeData !== undefined && this.currentRightClickNodeData !== null) {
        // console.log(this.currentRightClickNodeData);
        return this.currentRightClickNodeData.name;
      }
      return "";
    }
  },
  data() {
    return {
      formLabelWidth: '80px',
      timer: null,
      oldName: '',
      form: {},
    }
  },
  methods: {
    handleSubmit() {
      this.changeLoading(true);
      const that = this;
      const sql = 'drop table  `' + this.currentRightClickNodeData.name +"`";
      //
      that.$api.post('/exec/sensitive/sql', { sourceId: this.currentRightClickNodeData.schemaId, sql: sql }, async (res) => {
        if(res !== undefined && res.status !== undefined && res.status === 200) {
          if(res.data.code === '200') {
            this.openLayer('消息', res.data.data, 'success');
            // 关闭弹出层
            this.cancelForm();
            await this.refreshTree();
          } else {
            this.openLayer('消息', res.data.data, 'error');
          }
        } else {
          this.openLayer('消息', res.data, 'error');
        }
        this.changeLoading(false);
      });
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
  }
}
</script>

<style scoped>
.code-container {

}

.code-text {
  background: #2d2d2d;
  min-height: 50px;
  color: #ccc;
  border: 2px;
  word-break: break-all;
  white-space: pre;
  overflow-x: scroll;
  overscroll-behavior-x: contain;
  text-align: initial;
  /* 居中 */
  display: flex;
  align-items: center;           /*  垂直居中 */
  /*justify-content: center;*/  /*  水平居中  */
}
</style>