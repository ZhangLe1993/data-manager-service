<template>
  <div :id="divId">
    <AceEditor :ref="name"></AceEditor>
  </div>
</template>

<script>
import AceEditor from "@/components/AceEditor";
export default {
  name:'BaseEditor',
  props: {
    divId: String,
    name: String,
  },
  components: {
    AceEditor : AceEditor,
  },
};
</script>

<style scoped>

</style>