<template>
  <div :id="divId">
    <AceEditor :ref="name" :name="name" :item="item"></AceEditor>
  </div>
</template>

<script>
import AceEditor from "@/components/AceEditor";
export default {
  name:'BaseEditor',
  props: {
    divId: String,
    name: String,
    item: Object,
  },
  components: {
    AceEditor : AceEditor,
  },
};
</script>

<style scoped>

</style>