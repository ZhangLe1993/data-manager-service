<template>
  <el-dialog :title="connectTitle" :visible.sync="dialogFormVisible" :show-close="false" :close-on-click-modal="false" width="30%">

    <el-form :model="form" :rules="rules" ref="form">

      <el-form-item label="连接名称" :label-width="formLabelWidth" prop="name">
        <el-input v-model="form.name" auto-complete="off"></el-input>
      </el-form-item>

      <el-row>
        <el-col :span="16">
            <el-form-item label="主机地址" :label-width="formLabelWidth" prop="host">
              <el-input v-model="form.host" auto-complete="off"></el-input>
            </el-form-item>
        </el-col>
        <el-col :span="8">
            <el-form-item label="端口号" :label-width="formLabelWidth" prop="port">
              <el-input v-model="form.port" auto-complete="off"></el-input>
            </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="用户名" :label-width="formLabelWidth" prop="username">
        <el-input v-model="form.username" auto-complete="off"></el-input>
      </el-form-item>

      <el-form-item label="密码" :label-width="formLabelWidth" prop="password">
        <el-input v-model="form.password" auto-complete="off"></el-input>
      </el-form-item>

    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="onCancel">取 消</el-button>
      <el-button type="primary" @click="onSubmit('form')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "ConnectForm",
  props: {
    dialogFormVisible: {
      type: Boolean,
      default: false
    },
    cancel: Function,
    form: Object,
    treeRefresh:Function,
    currentClickNodeData: Object,
    connectTitle : String,
  },
  data() {
    return {
      formLabelWidth: '80px',
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
        ],
        host: [
          { required: true, message: '请输入主机地址', trigger: 'blur' },
        ],
        port: [
          { required: true, message: '请输入端口号', trigger: 'blur' },
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [{required: true, message: '请输入密码', trigger: 'blur' },],
      },
    }
  },
  methods: {
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
    onSubmit(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const that = this;
          const formData = that.form;
          if(formData.id === 0) {
            formData.parentId = -1;
            // 新增的时候绑定父节点
            if(this.currentClickNodeData != null) {
              formData.parentId = this.currentClickNodeData.id;
            }
            const config = { host : formData.host, port : formData.port, username : formData.username, password : formData.password };
            formData.config = JSON.stringify(config);
            // console.log(formData);
            // 新增
            that.$api.post('/connection', JSON.stringify(formData), (res) => {
              if(res !== undefined && res.status !== undefined && res.status === 200) {
                this.openLayer('消息', '恭喜你，新增成功。', 'success');
                // 关闭弹出层
                this.onCancel();
                this.treeRefresh();
              } else {
                this.openLayer('消息', res.data, 'error');
              }
            });
          } else {
            // 更新
            const config = { host : formData.host, port : formData.port, username : formData.username, password : formData.password };
            formData.config = JSON.stringify(config);
            that.$api.put('/connection', JSON.stringify(formData), (res) => {
              // console.log(res);
              if(res !== undefined && res.status !== undefined && res.status === 200) {
                this.openLayer('消息', '恭喜你，修改成功。', 'success');
                // 关闭弹出层
                this.onCancel();
                this.treeRefresh();
              } else {
                this.openLayer('消息', res.data, 'error');
              }
            });
          }
        } else {
          // console.log('error submit!!');
          return false;
        }
      });
    },
    onCancel() {
      this.cancel();
    },
  },
  async mounted() {

  }
}
</script>

<style scoped>
.el-select {
  display: block;
}
</style>