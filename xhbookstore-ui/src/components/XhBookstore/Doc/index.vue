<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'XhBookstoreDoc',
  data() {
    return {
      url: 'http://doc.xhbookstore.com/xhbookstore-vue'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>