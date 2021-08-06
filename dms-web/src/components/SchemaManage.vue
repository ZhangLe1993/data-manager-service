<template>
  <div class="manage">
    <el-row>
      <el-col :span="4">
        <el-tabs type="border-card">
          <Tree :addTab="addTab"></Tree>
        </el-tabs>
      </el-col>
      <el-col :span="20">
        <el-row>
          <el-col>
            <EditorTabs ref="editorTabs"></EditorTabs>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <div style="border: 1px solid #eee;height:35px;text-align: left;">
              <i @click="run" class="el-icon-caret-right select" style="float:left;margin-top:11px;margin-left:10px;margin-right:80px;cursor: pointer;">运行</i>

              <i @click="stop" class="el-icon-video-pause select" style="float:left;margin-top:11px;margin-right:80px;cursor: pointer;">停止</i>

              <i @click="addSelectTab" class="el-icon-circle-plus-outline select" style="float:left;margin-top:11px;margin-right:80px;cursor: pointer;">新窗口</i>

              <i @click="format" class="iconfont iconformat select" style="float:left;margin-top:6px;cursor: pointer;margin-right:250px;">格式化</i>

              <el-divider direction="vertical"></el-divider>
              <el-divider direction="vertical"></el-divider>
              <el-divider direction="vertical"></el-divider>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <OutputTabs ref="outputTabs"></OutputTabs>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Tree from "@/components/Tree";
import EditorTabs from "@/components/EditorTabs";
import OutputTabs from "@/components/OutputTabs";

export default {
  name: "SchemaManage",
  components: {
    Tree: Tree,
    EditorTabs: EditorTabs,
    OutputTabs: OutputTabs,
  },
  methods: {
    // 添加选项卡
    addTab(node) {
      this.$refs.editorTabs.addTab(node);
    },
    run() {
      // 先清除掉上一次的
      this.$refs.outputTabs.removeResultTab();
      const content = this.$refs.editorTabs.getContent();
      console.log(content);
      const that = this;
      that.$api.post('/exec/sql', { sourceId: 1, sql: content }, (res) => {
        console.log(res);
        if(res.status === 200) {
          const data = res.data.data;
          const code = res.data.code;
          if(code === '200') {
            for(let i = 0; i < data.length; i++) {
              const list = data[i]['resultList'];
              const columns = data[i]['columns'];
              console.log(list);
              // const tableHeader = that.$_.map(columns, 'name');
              // console.log(tableHeader);
              this.addResultTab(columns, list);
            }
          } else {
            this.setLogs(data);
          }
        }
      });
    },
    stop() {

    },
    addSelectTab() {
      const targetName = this.$refs.editorTabs.editableTabsValue
      this.$refs.editorTabs.addTab({name: "新建查询" + targetName});
    },
    addResultTab(tableHeader, tableData) {
      const targetName = this.$refs.outputTabs.editableTabsValue
      this.$refs.outputTabs.addTab(targetName, tableHeader, tableData);
    },
    format() {
      this.$refs.editorTabs.format();
      // console.log(this.$refs.editorTabs);
    },
    setLogs(content) {
      this.$refs.outputTabs.setRunLogs(content);
    },
  }
}
</script>

<style>

.manage .el-tabs__item {
  border: 2px solid #eee;
  background: #fff;
  color: #a6a6a6;
}

.manage .el-tabs--card>.el-tabs__header .el-tabs__item {
  border-top: 1px solid #eee !important;
  border-left: 1px solid #eee !important;
  border-right: 1px solid #eee !important;
}

.manage .el-tabs__item.is-active {
  color: #000;
  background: #fff;
  border-top: 1px solid #eee !important;
  border-left: 1px solid #eee !important;
  border-right: 1px solid #eee !important;
}

.manage .el-tabs__item:hover {
  color: #000;
  background: #fff;
  border-top: 1px solid #eee !important;
  border-left: 1px solid #eee !important;
  border-right: 1px solid #eee !important;
}

.manage .el-tabs__nav-scroll {
  background: #fff !important;
}
</style>