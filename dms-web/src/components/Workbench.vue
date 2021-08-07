<template>

  <el-row align="middle" type="flex"  style="flex-wrap: wrap">
    <el-col :lg="{ span:6 }" v-for="(item) in schemaList" :key="item.id" :offset="0" class="card-col-wb">

      <el-card @click.native="handleAddSchema" shadow="hover" class="card-add-btn" v-if="item['root'] !== undefined">
        <i class="el-icon-plus" style="margin-top: 30px"></i>
      </el-card>

      <el-card @dblclick.native="doubleClick(item)" class="box-card" v-else>
        <div slot="header" class="clearfix">
          <i class="el-icon-edit" style="cursor: pointer" @click="handleEditSchema(item)">编辑</i> &nbsp;&nbsp;&nbsp;&nbsp;
          <i class="el-icon-delete" style="cursor: pointer" @click="handleDeleteSchema(item)">删除</i>
        </div>
        <div>
          <span class="word">
            <span class="word-label">{{'实例名称: ' }}</span>
            <span class="word-weight">{{ item.name }}</span>
            <i class="el-icon-document word-copy-btn" title="点击复制" @click="copyText($event, item.name)"/>
          </span>
          <span class="word">
            <span class="word-label">{{'用户名: ' }}</span>
            <span class="word-weight">{{ item.username }}</span>
            <i class="el-icon-document word-copy-btn" title="点击复制" @click="copyText($event, item.username)"/>
          </span>
          <span class="word">
            <span class="word-label">{{'连接串: ' }}</span>
            <span class="word-weight" :title="item.url">{{ item.url }}</span>
            <i class="el-icon-document word-copy-btn" title="点击复制" @click="copyText($event, item.url)"/>
          </span>
        </div>
      </el-card>
    </el-col>

    <SeparateConnectForm :dialogFormVisible="connectFormVisible" :cancel="handleConnectFormCancel" :form="connectForm" :listRefresh="refresh" :connectTitle="connectTitle"/>
  </el-row>

</template>

<script>
import SeparateConnectForm from "@/components/SeparateConnectForm";
import Clipboard from 'clipboard'
export default {
  name: "Workbench",
  props: {
    addManageTab: Function,
  },
  components: {
    SeparateConnectForm: SeparateConnectForm,
  },
  data() {
    return {
      schemaList: [{ root: '添加' }],
      root: { root: '添加' },
      connectFormVisible: false,
      connectForm: {
        id: 0,
        name: '',
        url: '',
        username : '',
        password : '',
      },
      connectTitle: '新增连接',
    }
  },
  methods: {
    getConnectionList() {
      const that = this;
      that.$api.get('/separate/connection', { }, (res) => {
        let data = res.data;
        data.push(this.root);
        this.schemaList = data;
      });
    },
    refresh() {
      this.getConnectionList();
    },
    handleConnectFormCancel() {
      this.connectFormVisible = false;
      this.connectForm = {id: 0, name: '',  url : '', username : '', password : '' };
    },
    handleAddSchema() {
      this.connectTitle = '新增实例';
      // form 重置
      this.connectForm = {id: 0, name: '', url : '', username : '', password : '' };
      this.connectFormVisible = true;
    },
    handleEditSchema(item) {
      this.connectTitle = '编辑实例';
      this.connectForm = {id : item.id, name: item.name, url : item.url, username : item.username, password : item.password, };
      this.connectFormVisible = true;
    },
    handleDeleteSchema(item) {
      const that = this;
      this.$confirm('此操作将永久删除该实例, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        that.$api.delete('/connection', {id : item.id}, (res) => {
          if(res !== undefined && res.status !== undefined && res.status === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.refresh();
          } else {
            this.$message({
              type: 'info',
              message: '删除失败'
            });
          }
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    doubleClick(item) {
      this.addManageTab(item);
    },
    copyText(e, text) {
      const clipboard = new Clipboard(e.target, { text: () => text })
      clipboard.on('success', () => {
        this.$message({ type: 'success', message: '复制成功' })
        // 释放内存
        clipboard.off('error')
        clipboard.off('success')
        clipboard.destroy()
      })
      clipboard.on('error', () => {
        // 不支持复制
        this.$message({ type: 'waning', message: '该浏览器不支持自动复制' })
        // 释放内存
        clipboard.off('error')
        clipboard.off('success')
        clipboard.destroy()
      })
      clipboard.onClick(e)
    }
  },
  mounted() {
    this.getConnectionList();
  }

}
</script>

<style scoped>

.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 400px;
}
.card-col-wb {
  justify-content:center;
  align-items:center;
  display:flex;
  text-align: initial;
  margin-top:20px;
}
.card-add-btn {
  width:400px;
  height:257px;
  text-align: center;
  font-weight: bold;
  font-size: 100px;
  cursor: pointer;
}

.word {
  display: block;
  padding: 10px;
  color: rgba(22,24,35,0.5);
  width: 100%;
}

.word-weight {
  text-align: center;
  color: rgba(22,24,35,0.5);
  display:inline-block;
  width: 72%;
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
  background: rgba(46,50,56,0.05);
  height: 30px;
  line-height: 30px;
}
.word-label {
  display: inline-block;
  width: 20%;
  height: 30px;
  line-height: 30px;
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
}
.word-copy-btn {
  position: relative;
  float: right;
  font-size: 20px;
  margin-top: 5px;
  color: #409EFF;
}
</style>