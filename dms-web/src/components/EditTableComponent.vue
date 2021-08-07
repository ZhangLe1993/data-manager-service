<template>
  <el-table :data="tableData" size="mini">

    <el-table-column type="index" width="50"></el-table-column>

    <el-table-column property="name" label="字段名" width="150">
      <template slot-scope="scope">
        <el-input
            type="text"
            placeholder="请输入内容"
            v-model="scope.row.name"
            maxlength="64"
            show-word-limit
        >
        </el-input>
      </template>
    </el-table-column>

    <el-table-column property="comment" label="描述">
      <template slot-scope="scope">
        <el-input
            type="text"
            placeholder="请输入描述"
            v-model="scope.row.comment"
            maxlength="255"
            show-word-limit
        >
        </el-input>
      </template>
    </el-table-column>

    <el-table-column property="type" label="类型">
      <template slot-scope="scope">
        <el-select v-model="scope.row.type" placeholder="请选择">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
      </template>
    </el-table-column>

    <el-table-column property="len" label="长度">
      <template slot-scope="scope">
        <el-input
            type="number"
            placeholder="请输入内容"
            v-model="scope.row.len"
            min="0"
        >
        </el-input>
      </template>
    </el-table-column>
    <el-table-column property="canVoid" label="可否为空" width="100">
      <template slot-scope="scope">
        <el-checkbox v-model="scope.row.canVoid"></el-checkbox>
      </template>
    </el-table-column>
    <el-table-column property="pri" label="是否主键" width="100">
      <template slot-scope="scope">
        <el-checkbox v-model="scope.row.pri"></el-checkbox>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: "EditTableComponent",
  data() {
    return {
      tableData: [{
        name: 'id',
        comment: '主键',
        type:'INT UNSIGNED',
        len: 20,
        canVoid: false,
        pri: true,
      },
        {
          name: 'desc',
          comment: '描述',
          type:'VARCHAR',
          len: 255,
          canVoid: true,
          pri: false,
        },
        {
          name: 'host',
          comment: '主机名称',
          type:'VARCHAR',
          len: 255,
          canVoid: false,
          pri: false,
        },
        {
          name: 'port',
          comment: '端口号',
          type:'INT',
          len: 4,
          canVoid: false,
          pri: false,
        },
        {
          name: 'username',
          comment: '用户名',
          type:'VARCHAR',
          len: 25,
          canVoid: false,
          pri: false,
        }
      ],
      options: [{
        value: 'VARCHAR',
        label: 'VARCHAR'
      }, {
        value: 'INT',
        label: 'INT'
      },{
        value: 'INT UNSIGNED',
        label: 'INT UNSIGNED'
      }, {
        value: 'DOUBLE',
        label: 'DOUBLE'
      }, {
        value: 'DATE',
        label: 'DATE'
      }, {
        value: 'TEXT',
        label: 'TEXT'
      },
        {
          value: 'TIMESTAMP',
          label: 'TIMESTAMP'
        }
      ],
    };
  }
}
</script>

<style scoped>

</style>