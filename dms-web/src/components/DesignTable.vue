<template>
  <el-dialog :title="getTitleName" :visible.sync="designTableVisible" width="80%" top="8vh" class="design" :show-close="false" :close-on-click-modal="false">
    <el-collapse accordion v-model="activeNames">
      <el-collapse-item name="baseInfo">
        <template slot="title">
          <h3>基本信息</h3>
        </template>
        <el-form label-position="right" label-width="100px" :model="baseInfo" ref="form" :rules="rules">
        基础选项
        <div class="design-base-info">
          <div style="width:60%; margin-left: 20px;">
              <el-form-item label="表名:" prop="tableName">
                <el-input v-model="baseInfo.tableName" style="width:60%" :disabled="editOpt"></el-input>
                &nbsp;&nbsp;
                <i class="el-icon-question" style="cursor: pointer;font-size: 20px;" @click="open3('请谨慎修改表名')"></i>
              </el-form-item>
              <el-form-item label="备注:">
                <el-input v-model="baseInfo.tableComment" style="width:60%"></el-input>
              </el-form-item>
          </div>

        </div>
        更多选项
        <div class="design-base-info">
          <div style="width:60%; margin-left: 20px;">
            <el-form-item label="字符集:">
              <el-select v-model="baseInfo.tableCharacter" clearable placeholder="一旦保存不可更改" style="width:60%" :disabled="editOpt">
                <el-option label="utf8" value="utf8"></el-option>
                <el-option label="utf8mb4" value="utf8mb4"></el-option>
                <el-option label="gbk" value="gbk"></el-option>
              </el-select>
              &nbsp;&nbsp;
              <i class="el-icon-question" style="cursor: pointer;font-size: 20px;" @click="open3('字符集一旦保存不可更改')"></i>
            </el-form-item>
            <el-form-item label="自增起始值:">
              <el-input v-model="baseInfo.tableAutoIncrementNum" placeholder="不填,采用默认值" style="width:60%" :disabled="editOpt"></el-input>
              &nbsp;&nbsp;
              <i class="el-icon-question" style="cursor: pointer;font-size: 20px;" @click="open3('不需要变更表的起始值时,请不要修改')"></i>
            </el-form-item>
          </div>
        </div>
        </el-form>
      </el-collapse-item>

      <el-collapse-item name="fieldInfo">
        <template slot="title">
          <h3>字段</h3>
        </template>
        <el-button type="primary" size="mini" @click="addRow">增加</el-button>
        <el-button type="danger" size="mini" @click="delRow">删除</el-button>
        <el-divider></el-divider>
        <el-table
            v-loading="loading"
            element-loading-text="拼命加载中"
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
            :data="tableData"
            @row-click="fieldTableRowClick"
            border
            size="mini"
        >

          <el-table-column type="index" label="#" width="50"></el-table-column>

          <el-table-column property="name" label="字段名" width="350">
            <template slot-scope="scope">
              <el-input
                  type="text"
                  placeholder="字段名"
                  v-model="scope.row.name"
                  maxlength="64"
                  show-word-limit
              >
              </el-input>
            </template>
          </el-table-column>

          <el-table-column property="comment" label="描述" width="450">
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
              <el-select v-model="scope.row.type" clearable placeholder="请选择">
                <el-option
                    v-for="item in dataTypeOptions"
                    :key="item"
                    :label="item"
                    :value="item">
                </el-option>
              </el-select>
            </template>
          </el-table-column>

          <el-table-column property="len" label="长度">
            <template slot-scope="scope" v-if="!notAllowEditLenArr.includes(scope.row.type)">
              <el-input
                  type="number"
                  placeholder="非必填"
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
          <!--<el-table-column property="pri" label="是否主键" width="100">
            <template slot-scope="scope">
                <el-checkbox v-model="scope.row.pri"></el-checkbox>
            </template>
          </el-table-column>-->
        </el-table>
        <br>
        <div v-if="currentSelectRow !== null && dateTimeTemplateArr.includes(currentSelectRow.type)">
          <h3 style="background: #42b983; line-height: 50px">扩展属性</h3>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">更新策略: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-checkbox > on update CURRENT_TIMESTAMP</el-checkbox>
            </div>
          </div>
          <br>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">默认值: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-input v-model="currentSelectRow['defaultValue']" placeholder="格式: YYYY-MM-DD HH:MM:SS[.fraction]" style="width:500px;"></el-input>
            </div>
          </div>
        </div>
        <!-- int 型推荐 -->
        <div v-if="currentSelectRow !== null && intTemplateArr.includes(currentSelectRow.type)">
          <h3 style="background: #42b983; line-height: 50px">扩展属性</h3>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">默认值: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-input placeholder="值区间: [0, 18446xxxxx]" v-model="currentSelectRow['defaultValue']" style="width:500px;"></el-input>
            </div>
          </div>
          <br>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">自动增长: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-checkbox v-model="currentSelectRow.autoIncrement"></el-checkbox>
            </div>
          </div>
        </div>

        <!-- DECIMAL 类型-->
        <div v-if="currentSelectRow !== null && currentSelectRow.type === 'DECIMAL'">
          <h3 style="background: #42b983; line-height: 50px">扩展属性</h3>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">默认值: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-input v-model="currentSelectRow['defaultValue']" placeholder="" style="width:500px;"></el-input>
            </div>
          </div>
          <br>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">小数点: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-input placeholder="" v-model="currentSelectRow.decimalDigits" style="width:500px;"></el-input>
            </div>
          </div>
        </div>
        <!-- VARCHAR 类型-->
        <div v-if="currentSelectRow !== null && currentSelectRow.type === 'VARCHAR'">
          <h3 style="background: #42b983; line-height: 50px">扩展属性</h3>
          <div style="display: flex;align-items: center; justify-content: center;">
            <div style="width: 200px;text-align: right">默认值: </div>
            <div style="width:600px;">
              &nbsp;&nbsp;<el-input v-model="currentSelectRow['defaultValue']" placeholder="" style="width:500px;"></el-input>
            </div>
          </div>
        </div>
      </el-collapse-item>

      <el-collapse-item name="uniqueInfo">
        <template slot="title">
          <h3>索引</h3>
        </template>
        <el-button type="primary" size="mini" @click="addIndex">增加</el-button>
        <el-button type="danger" size="mini">删除</el-button>
        <el-divider></el-divider>
        <el-table
            :data="indexTableData"
            border
            size="mini"
        >

          <el-table-column type="index" label="#" width="50"></el-table-column>

          <el-table-column property="indexName" label="索引名" >
            <template slot-scope="scope">
              <el-input
                  type="text"
                  placeholder="索引名"
                  v-model="scope.row.indexName"
                  maxlength="64"
                  show-word-limit
              >
              </el-input>
            </template>
          </el-table-column>

          <el-table-column property="indexType" label="索引类型" width="300">
            <template slot-scope="scope">
              <el-select v-model="scope.row.indexType" clearable placeholder="索引类型" style="width:100%">
                <el-option
                    v-for="item in indexOptions"
                    :key="item"
                    :label="item"
                    :value="item">
                </el-option>
              </el-select>
            </template>
          </el-table-column>


          <el-table-column property="indexColumns" label="索引包含列">
            <template slot-scope="scope">
              <el-select
                  v-model="scope.row.indexColumns"
                  multiple
                  filterable
                  default-first-option
                  placeholder="请选择索引包含列"  style="width:100%">
                <el-option
                    v-for="item in getFieldOptions"
                    :key="item"
                    :label="item"
                    :value="item">
                </el-option>
              </el-select>

            </template>
          </el-table-column>

        </el-table>
      </el-collapse-item>
    </el-collapse>

    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="handleSubmit('form')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import constructSQL from "@/utils/sqlGenerateUtil.js";
import getIndex from "@/utils/utils";
export default {
  name: "DesignTable",
  props: {
    designTableVisible: {
      type: Boolean,
      default: false
    },
    closeDialog: Function,
    tableInfo: Object,
    editOpt: Boolean,
  },
  watch: {
    /**
     * 监听当前值的变化,动态更新到 tableData数组中
     * */
    "currentSelectRow.defaultValue": {
      handler: function() {
        console.log(this.currentSelectRow);
        const index = getIndex(this.currentSelectRow, this.tableData);
        this.tableData[index] = this.currentSelectRow;
      }
    },
    "currentSelectRow.autoIncrement": {
      handler: function() {
        console.log(this.currentSelectRow);
        const index = getIndex(this.currentSelectRow, this.tableData);
        this.tableData[index] = this.currentSelectRow;
      }
    },
    "currentSelectRow.decimalDigits": {
      handler: function() {
        console.log(this.currentSelectRow);
        const index = getIndex(this.currentSelectRow, this.tableData);
        this.tableData[index] = this.currentSelectRow;
      }
    }
  },
  computed: {
    getTableName() {
      if(this.tableInfo !== undefined && this.tableInfo !== null) {
        return this.tableInfo.name;
      }
      return "";
    },
    getTitleName() {
      if(this.tableInfo !== undefined && this.tableInfo !== null) {
        return "设计表[" + this.tableInfo.name+"]";
      }
      return "新建表";
    },
    getFieldOptions() {
      // eslint-disable-next-line no-unused-vars
      return this.tableData.map((item, _) => {
        return item.name;
      });
    },
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
        // int 类型有 扩展属性
        let extend = { defaultValue: '' };
        if(this.intTemplateArr.includes(obj.type)) {
          extend['autoIncrement'] = false;
        }
        arr.push({
          name: obj.name,
          comment: obj.comment,
          type: obj.type,
          len: obj.size,
          canVoid: obj.nullAble === 'YES',
          pri: primaryKeys.includes(obj.name),
          autoIncrement: obj.autoIncrement === 'YES',
          decimalDigits: obj.decimalDigits,
          defaultValue: obj.defaultValue,
          numPrecRadix: obj.numPrecRadix,
        });
      }
      this.tableData = arr;
      // eslint-disable-next-line no-unused-vars
      this.fieldOptions = this.tableData.map((item, _) => {
        return item.name;
      });
      let baseInfoTemp = {
        tableName: this.tableInfo.name,
        tableEngine: this.tableInfo.engine,
        tableComment: this.tableInfo.comment,
        tableCharacter: this.tableInfo.charset,
        tableAutoIncrementNum: this.tableInfo.autoIncrementNum,
      };
      this.baseInfo = baseInfoTemp;
    }
    this.loading = false;
  },
  data() {
    return {
      // 当前是否是更新操作

      activeNames: ['baseInfo'],
      baseInfo: {
        tableName: '',
        tableEngine: 'InnoDB',
        tableComment: '',
        tableCharacter: '',
        tableAutoIncrementNum: '',
      },
      rules: {
        tableName: [
          { required: true, message: '请输入表名', trigger: 'blur' },
          { min: 1, max: 64, message: '长度在 1 到 64 个字符', trigger: 'blur' }
        ],
      },
      dateTimeTemplateArr: ['DATETIME', 'TIMESTAMP'],
      intTemplateArr:['INT', 'BIGINT', 'INT UNSIGNED', 'BIGINT UNSIGNED'],
      notAllowEditLenArr:['TEXT', 'TIMESTAMP', 'DATE', 'DATETIME', 'BLOB', 'CLOB', 'LONGTEXT', 'INT', 'BIGINT', 'INT UNSIGNED', 'BIGINT UNSIGNED'],
      loading: true,
      currentSelectRow: null,
      tableData: [{
        name: 'id',
        comment: '主键',
        type:'BIGINT UNSIGNED',
        len: 20,
        canVoid: false,
        pri: true,
        autoIncrement: true,
        decimalDigits: 0,
        defaultValue: null,
        numPrecRadix: 10,
      },
        {
          name: 'gmt_create',
          comment: '创建时间',
          type:'DATETIME',
          len: '',
          canVoid: false,
          pri: false,
          autoIncrement: false,
          decimalDigits: 0,
          defaultValue: 'CURRENT_TIMESTAMP',
          numPrecRadix: 10,
        },
        {
          name: 'gmt_update',
          comment: '修改时间',
          type:'DATETIME',
          len: '',
          canVoid: false,
          pri: false,
          autoIncrement: false,
          decimalDigits: 0,
          defaultValue: 'CURRENT_TIMESTAMP',
          numPrecRadix: 10,
        }
      ],
      indexTableData: [
        {
          indexName: 'PRIMARY',
          indexType: 'Primary',
          indexColumns: ['id'],
        }
      ],
      formLabelWidth: '120px',
      indexOptions: ['Primary', 'Normal', 'Unique', 'FullText', 'Spatial'],
      fieldOptions: [],
      dataTypeOptions:['TEXT', 'TIMESTAMP', 'DATE', 'DATETIME', 'BLOB', 'CLOB', 'LONGTEXT', 'INT', 'DECIMAL', 'BIGINT', 'BOOLEAN', 'INT UNSIGNED', 'BIGINT UNSIGNED', 'VARCHAR'],
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
    open3(text) {
      this.$message({
        message: text,
        type: 'warning'
      });
    },
    fieldTableRowClick(row, column, event) {
      console.log(row, column, event);
      this.currentSelectRow = row;
    },
    addRow() {
      this.tableData.push({
        name: '',
        comment: '',
        type:'VARCHAR',
        canVoid: true,
        pri: false,
        autoIncrement: false,
        decimalDigits: 0,
        defaultValue: null,
        numPrecRadix: 10,
      });
    },
    delRow() {
      if(this.currentSelectRow === undefined || this.currentSelectRow === null) {
        this.open3("请先单击某行进行选中");
        return;
      }
      this.tableData = this.tableData.filter((item) => { return item.name !== this.currentSelectRow.name });
      // 重设置为null, 或者重置为下一行
      this.currentSelectRow = null;
    },
    addIndex() {
      this.indexTableData.push({
        indexName: '',
        indexType: '',
        indexColumns: [],
      });
    },
    handleSubmit(formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const sql = constructSQL(this.baseInfo, this.tableData, this.indexTableData);
          console.log(sql);
        } else {
          this.open3("请检查必填参数");
          return false;
        }
      });
    },
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
  }
}
</script>

<style>
.design .el-loading-mask {
  height: 100%;
}
.design {
  text-align: initial;
}
.design-base-info {
  width: 99%;
  height: 200px;
  border: 1px dashed #cccccc;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.design .el-dialog {
  max-height: 80vh;
  overflow-y: auto;
}
</style>