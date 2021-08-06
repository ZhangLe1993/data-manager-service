<template>
  <el-dialog :title="connectTitle" :visible.sync="dialogFormVisible" :show-close="false" :close-on-click-modal="false" width="30%">

    <el-form :model="form" :rules="rules" ref="form">

      <el-form-item label="实例名称" :label-width="formLabelWidth" prop="name">
        <el-input v-model="form.name" auto-complete="off"></el-input>
      </el-form-item>

      <el-form-item label="连接串" :label-width="formLabelWidth" prop="url">
        <el-input v-model="form.url" auto-complete="off"></el-input>
      </el-form-item>

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
      <el-button type="primary" @click="onSubmit('form')">测 试</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "SeparateConnectForm",
  props: {
    dialogFormVisible: {
      type: Boolean,
      default: false
    },
    cancel: Function,
    form: Object,
    listRefresh:Function,
    connectTitle : String,
  },
  data() {
    return {
      formLabelWidth: '80px',
      rules: {
        name: [
          { required: true, message: '请输入实例名称', trigger: 'blur' },
          { min: 1, max: 500, message: '长度在 1 到 255 个字符', trigger: 'blur' }
        ],
        url: [
          { required: true, message: '例如 jdbc:mysql://127.0.0.1:3306/test?xxxxx', trigger: 'blur' },
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
          formData.mode = 'separate';
          if(formData.id === 0) {
            const config = { url : formData.url, username : formData.username, password : formData.password };
            formData.config = JSON.stringify(config);

            console.log(formData);
            // 新增
            that.$api.post('/connection', JSON.stringify(formData), (res) => {
              if(res !== undefined && res.status !== undefined && res.status === 200) {
                this.openLayer('消息', '恭喜你，新增成功。', 'success');
                // 关闭弹出层
                this.onCancel();
                this.listRefresh();
              } else {
                this.openLayer('消息', res.data, 'error');
              }
            });
          } else {
            // 更新
            const config = { url : formData.url, username : formData.username, password : formData.password };
            formData.config = JSON.stringify(config);
            that.$api.put('/connection', JSON.stringify(formData), (res) => {
              console.log(res);
              if(res !== undefined && res.status !== undefined && res.status === 200) {
                this.openLayer('消息', '恭喜你，修改成功。', 'success');
                // 关闭弹出层
                this.onCancel();
                this.listRefresh();
              } else {
                this.openLayer('消息', res.data, 'error');
              }
            });
          }
        } else {
          console.log('error submit!!');
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