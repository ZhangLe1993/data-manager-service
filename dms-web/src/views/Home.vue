<template>
  <div class="home">
    <!--<div style="margin-bottom: 20px;margin-top: 20px;">
      <el-button
          type="primary"
          @click="addTab(editableTabsValue)"
      >
        新增选项卡
      </el-button>
    </div>-->
    <el-tabs v-model="editableTabsValue" type="card" @tab-remove="removeTab" @tab-click="switchTab">
      <el-tab-pane
          v-for="(item, index) in editableTabs"
          :key="item.name"
          :label="item.title"
          :name="item.name"
          :index="index"
          :closable="item.closable"
      >
        <!-- 工作台 -->
        <Workbench v-if=" item.name === '1'" :addManageTab="addManageTab"></Workbench>
        <SchemaManage v-if="item.name !== '1'" :schemaItem="selectSchemaItem"> </SchemaManage>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Workbench from "@/components/Workbench";
import SchemaManage from "@/components/SchemaManage";
export default {
  name: "Home",
  components: {
    Workbench: Workbench,
    SchemaManage: SchemaManage,
  },
  data() {
    return {
      editableTabsValue: '1',
      editableTabs: [{
        title: '工作台',
        name: '1',
        closable: false,
        schemaItem: null,
      }],
      tabIndex: 1,
      activeName: 1,
      selectSchemaItem: null,
    }
  },
  methods: {
    /**
     * 添加选项卡
     */
    addManageTab(schemaItem) {
      // console.log(schemaItem);
      let newTabName = ++this.tabIndex + '';
      this.editableTabs.push({
        title: 'SQL ' + schemaItem.name + ' ',
        name: newTabName,
        closable: true,
        schemaItem: schemaItem,
      });
      this.activeName = newTabName;
      this.editableTabsValue = newTabName;
      this.selectSchemaItem = schemaItem;
    },
    /**
     * 移除选项卡
     * */
    removeTab(targetName) {
      let tabs = this.editableTabs;
      let activeName = this.editableTabsValue;
      let selectItem = null;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              activeName = nextTab.name;
              selectItem = nextTab.schemaItem;
            }
          }
        });
      }
      this.activeName = activeName;
      this.editableTabsValue = activeName;
      this.selectSchemaItem = selectItem;
      this.editableTabs = tabs.filter(tab => tab.name !== targetName);
    },
    /**
     * 切换选项卡
     * @param tab
     */
    switchTab(tab) {
      // 更改当前选中的选项卡
      // this.activeName = tab.name;
      this.selectSchemaItem = tab.schemaItem;
      let tabs = this.editableTabs;
      tabs.forEach((tabItem, index) => {
        if (tabItem.name === tab.name) {
          let selectTab = tabs[index];
          if (selectTab) {
            this.activeName = selectTab.name;
            this.selectSchemaItem = selectTab.schemaItem;
          }
        }
      });
    },
  }
}
</script>

<style>

.home .el-tabs--card>.el-tabs__header .el-tabs__nav {
  border: 0 !important;
}

.home .el-tabs__item {
  margin-right: 3px;
  border-top-left-radius: 20px !important;
  border-top-right-radius: 20px !important;
  border: 2px solid #eee;
  background: #797979;
  color: #a6a6a6;
  padding: 0 40px;
}

.home .el-tabs--card>.el-tabs__header .el-tabs__item.is-closable:hover {
  padding-left: 40px;
  padding-right: 40px;
}

.home .el-tabs--top.el-tabs--card>.el-tabs__header .el-tabs__item:last-child {
  padding-left: 40px;
  padding-right: 40px;
}

.home .el-tabs--top.el-tabs--card>.el-tabs__header .el-tabs__item:nth-child(2) {
  padding-left: 40px;
  padding-right: 40px;
}

.home .el-tabs--card>.el-tabs__header .el-tabs__item.is-active.is-closable {
  padding-left: 40px;
  padding-right: 40px;
}

.home .el-tabs--card>.el-tabs__header .el-tabs__item .el-icon-close {
  width: 14px;
  right: -30px;
}

.home .manage .el-tabs--card>.el-tabs__header .el-tabs__item {
  border-top: 1px solid #eee !important;
  border-left: 1px solid #eee !important;
  border-right: 1px solid #eee !important;
}

.home .el-tabs--card>.el-tabs__header .el-tabs__item {
  border-bottom: 0;
  border-top: 1px solid #797979;
  border-left: 1px solid #797979 !important;
  border-right: 1px solid #797979;
}

.home .el-tabs__item.is-active {
  color: #000;
  background: #fff;
  border-top: 1px solid #a6a6a6;
  border-left: 1px solid #a6a6a6 !important;
  border-right: 1px solid #a6a6a6;
}

.home .el-tabs__item:hover {
  color: #000;
  background: #fff;
  border-top: 1px solid #a6a6a6;
  border-left: 1px solid #a6a6a6 !important;
  border-right: 1px solid #a6a6a6;
}
.home .el-tabs__nav-scroll {
  background: rgb(51, 51, 51);
}

body {
  margin: 0 !important;
}
</style>