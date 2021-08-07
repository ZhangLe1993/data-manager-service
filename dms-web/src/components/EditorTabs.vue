<template>
  <el-tabs v-model="editableTabsValue" type="card" @tab-remove="removeTab" @tab-click="switchTab">
    <el-tab-pane
        v-for="(item, index) in editableTabs"
        :key="item.name"
        :label="item.title"
        :name="item.name"
        :index="index"
        :closable="item.closable"
    >
      <AceEditor v-if="item.name === '1'" :ref="item.name"></AceEditor>
      <BaseEditor v-if="item.name !== '1'" :ref="item.name" :divId="item.divId" :name="item.name" :item="item"></BaseEditor>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import BaseEditor from '../components/BaseEditor';
import AceEditor from '../components/AceEditor';
export default {
  name: "EditorTabs",
  components: {
    BaseEditor: BaseEditor,
    AceEditor: AceEditor,
  },
  data() {
    return {
      editableTabsValue: '1',
      editableTabs: [{
        title: '查询窗口',
        name: '1',
        closable: false,
        divId: 'init_window',
      }],
      tabIndex: 1,
      // 激活的标签
      activeName: 1,
    }
  },
  methods: {
    addTab(node) {
      // console.log(node);
      let newTabName = ++this.tabIndex + '';
      this.editableTabs.push({
        title: 'Query ' + node.name,
        name: newTabName,
        closable: true,
        divId: "editor_" + newTabName,
        node: node,
      });
      this.editableTabsValue = newTabName;
      this.activeName = newTabName;
    },
    removeTab(targetName) {
      let tabs = this.editableTabs;
      let activeName = this.editableTabsValue;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              activeName = nextTab.name;
            }
          }
        });
      }
      this.editableTabsValue = activeName;
      this.activeName = activeName;
      this.editableTabs = tabs.filter(tab => tab.name !== targetName);
    },
    switchTab(tabs) {
      // 更改当前选中的选项卡
      this.activeName = tabs.name;
    },
    getContent() {
      // 获取当前选中的选项卡的编辑器内容
      if(this.activeName === '1' || this.activeName === 1) {
        return this.$refs[this.activeName][0].getContent();
      } else {
        return this.$refs[this.activeName][0].$refs[this.activeName].getContent();
      }
    },
    format() {
      // console.log(this.$refs[this.activeName][0]);
      if(this.activeName === '1' || this.activeName === 1) {
        this.$refs[this.activeName][0].format();
      } else {
        this.$refs[this.activeName][0].$refs[this.activeName].format();
      }
    }
  },
  mounted() {

  }
}
</script>

<style scoped>

</style>