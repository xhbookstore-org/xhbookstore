<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'XhBookstoreGit',
  data() {
    return {
      url: 'https://gitee.com/y_project/xhbookstore-Vue'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>