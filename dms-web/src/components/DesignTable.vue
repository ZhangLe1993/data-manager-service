<template>
  <el-dialog :title="'设计表[' + getTableName + ']'" :visible.sync="designTableVisible" class="design" :show-close="false" :close-on-click-modal="false">
    <el-table
        v-loading="loading"
        element-loading-text="拼命加载中"
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
        :data="tableData"
        size="mini"
    >

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
              :disabled="notAllowEditLenArr.includes(scope.row.type)"
          >
          </el-input>
        </template>
      </el-table-column>
      <el-table-column property="canVoid" label="可空" width="50">
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
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="closeDialog">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "DesignTable",
  props: {
    designTableVisible: {
      type: Boolean,
      default: false
    },
    closeDialog: Function,
    tableInfo: Object
  },
  computed: {
    getTableName() {
      if(this.tableInfo !== undefined && this.tableInfo !== null) {
        return this.tableInfo.name;
      }
      return "";
    }
  },
  async mounted() {
    console.log('kaishi ');
    this.loading = true;
    if(this.tableInfo !== undefined && this.tableInfo !== null) {
      const fieldInfo = await this.getFields(this.tableInfo.schemaId, this.tableInfo.schema, this.tableInfo.name);
      // eslint-disable-next-line no-case-declarations
      const fields = fieldInfo.columns;
      const primaryKeys = fieldInfo.primaryKeys;
      const arr = [];
      for(let i = 0; i < fields.length; i++) {
        const obj = fields[i];
        arr.push({
          name: obj.name,
          comment: obj.comment,
          type: obj.type,
          len: obj.size,
          canVoid: obj.nullAble === 'YES',
          pri: primaryKeys.includes(obj.name)
        });
      }
      this.tableData = arr;
    }
    this.loading = false;
  },
  data() {
    return {
      notAllowEditLenArr:['TEXT', 'TIMESTAMP', 'DATE', 'BLOB', 'CLOB', 'LONGTEXT',],
      loading: true,
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
        }],
      formLabelWidth: '120px',
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
  },
  methods: {
    async getFields(id, schema, table) {
      const that = this;
      const res = await that.$api.syncGet('/connection/schema/table/fields', { id : id, schema: schema, table : table});
      // console.log(res);
      if(res.status === 200) {
        return res.data;
      } else {
        return [];
      }
    },
  }
}
</script>

<style>
.design .el-loading-mask {
  height: 100%;
}
</style>