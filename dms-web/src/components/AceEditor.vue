<template>
    <editor id="editor" v-model="content" @init="editorInit" lang="sql" theme="chrome" width="100%" height="350"></editor>
</template>

<script>

// 引入 sql Format 组件
import sqlFormatter from 'sql-formatter';

export default {
    name: "AceEditor",
    props: {
      item: Object
    },
    components: {
        editor: require('vue2-ace-editor'),
    },
    data() {
        return {
            content: '',
        }
    },
    methods: {
        editorInit() {
            require('brace/ext/language_tools') //language extension prerequsite...
            // 语言
            require('brace/mode/sql')    //language
            // 主题
            require('brace/theme/chrome')
            // 自动补全
            require('brace/snippets/sql') //snippet
        },
        getContent() {
          return this.content;
        },
        format() {
          this.content = sqlFormatter.format(this.content);
        }
    },
    mounted () {
      if(this.item !== undefined &&  this.item !== null &&  this.item['title'] !== undefined &&  this.item['title'] !== null &&  this.item['title'].trim() !== '') {
        if(this.item['title'].indexOf('新建查询') !== -1) {
          this.content = 'SELECT * ';
        } else {
          this.content = 'SELECT * FROM `' + this.item.title.replace("Query ", "") + "`" + " limit 100;";
        }
      } else {
        this.content = '';
      }
    },
}
</script>

<style scoped>

</style>