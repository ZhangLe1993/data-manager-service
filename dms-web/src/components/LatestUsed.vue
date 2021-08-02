<template>
  <el-main style="overscroll-behavior-y: auto">
    <AceEditor ></AceEditor>
  </el-main>
</template>

<script>
import AceEditor from "@/components/AceEditor";
    export default {
      name: "LatestUsed",
      components: {
        AceEditor: AceEditor,
      },
      data() {
        return {
          activeIndex: '1',
          currentDate: ''
        };
      },
      methods: {
        handleSelect(key, keyPath) {
          console.log(key, keyPath);
        }
      }
    }
</script>

<style scoped>

</style>