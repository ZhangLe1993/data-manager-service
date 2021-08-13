<template>
  <el-drawer
      title="重命名"
      :before-close="cancelForm"
      :visible.sync="dialog"
      direction="rtl"
      custom-class="demo-drawer"
      ref="drawer"
  >
    <div class="demo-drawer__content">
      <el-form :model="form" ref="form">
        <el-form-item label="新表名:" :label-width="formLabelWidth">
          <el-input v-model="form.name" autocomplete="off" maxlength="64" show-word-limit></el-input>
        </el-form-item>
        <el-form-item label="预览:" :label-width="formLabelWidth">
          <div class="code-container">
            <div class="code-text">
              &nbsp; | <span codex="sql">alter table `{{ getOldName }}` rename to `{{ form.name }}`;</span>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <div class="demo-drawer__footer">
        <el-button @click="cancelForm">取 消</el-button>
        <el-button type="primary" @click="handleSubmit('form')" :loading="loading">{{ loading ? '提交中 ...' : '确 定' }}</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
export default {
  name: "RenameTableDrawer",
  props: {
    dialog: Boolean,
    loading: Boolean,
    cancelForm: Function,
    currentRightClickNodeData: Object,
    form: Object,
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
    }
  },
  methods: {
    handleSubmit(formName) {
      this.changeLoading(true);
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const that = this;
          const formData = that.form;
          if(formData.name === this.currentRightClickNodeData.name) {
            this.openLayer('消息', "与旧表名相同", 'warning');
            this.changeLoading(false);
            return false;
          }
          if(formData.name === undefined || formData.name === null || formData.name.trim() === '') {
            this.openLayer('消息', "请填写新表名", 'warning');
            this.changeLoading(false);
            return false;
          }
          const sql = 'alter table  `' + this.currentRightClickNodeData.name + '` rename to `' + formData.name.trim() +'`';
          // 新增
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
        } else {
          this.changeLoading(false);
          return false;
        }
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