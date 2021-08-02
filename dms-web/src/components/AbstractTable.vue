<template>
  <el-table
      :data="tableData"
      height="190"
      border
      size="mini"
      style="width: 100%">
    <template v-for="(item,index) in tableHeader">
      <el-table-column :prop="item.name" :label="item.name" :key="index"></el-table-column>
    </template>
  </el-table>
</template>

<script>
  export default {
    name: "AbstractTable",
    props: {
      tableHeader: Array,
      tableData: Array,
    },
    data() {
      return {

      }
    },
    methods: {

    }

  }
</script>

<style scoped>

</style>