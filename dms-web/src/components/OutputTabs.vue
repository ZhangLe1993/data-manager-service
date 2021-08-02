<template>
    <el-tabs v-model="editableTabsValue" type="card" @tab-remove="removeTab">
        <el-tab-pane
                v-for="(item, index) in editableTabs"
                :key="item.name"
                :label="item.title"
                :name="item.name"
                :index="index"
                :closable="item.closable"
        >
            <!--{{item.content}}-->
          <div v-if="item.name === '1'"> {{item.content}} </div>
          <AbstractTable v-if="item.name !== '1'" :ref="index" :tableHeader="item.tableHeader" :tableData="item.tableData"></AbstractTable>
        </el-tab-pane>
    </el-tabs>
</template>

<script>

    import AbstractTable from '../components/AbstractTable'; // 引入子组件AgentForm
    export default {
        name: "OutputTabs",
        components: {
          AbstractTable : AbstractTable,
        },
        data() {
            return {
                editableTabsValue: '1',
                editableTabs: [{
                    title: '#运行日志',
                    name: '1',
                    closable: false,
                    content: '',
                }],
                tabIndex: 1,
              // 激活的标签
              activeName: 1,
            }
        },
        methods: {
            addTab(targetName, tableHeader, tableData) {
                console.log(targetName);
                let newTabName = ++this.tabIndex + '';
                this.editableTabs.push({
                    title: '#结果集',
                    name: newTabName,
                    closable: true,
                    tableHeader: tableHeader,
                    tableData: tableData,
                });
                this.activeName = newTabName;
                this.editableTabsValue = newTabName;
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
            switchTab(tabsCpt) {
              // 更改当前选中的选项卡
              this.activeName = tabsCpt.name;
            },
            removeResultTab() {
              // 每次执行RUN方法要清空查询结果集的Tab
              let tabs = this.editableTabs;
              let activeName = this.editableTabsValue;
              tabs.forEach((tab, index) => {
                if (tab.title !== '#结果集') {
                  let nextTab = tabs[index + 1] || tabs[index - 1];
                  if (nextTab) {
                    activeName = nextTab.name;
                  }
                }
              });
              this.editableTabsValue = activeName;
              this.activeName = activeName;
              this.editableTabs = tabs.filter(tab => tab.title !== '#结果集');
            },
            setRunLogs(content) {
              this.editableTabs[0].content = content;
            }
        }
    }
</script>

<style scoped>

</style>