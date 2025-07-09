<template>
  <div class="">



    这是普通用户界面
    <el-button @click="goToBackground">去后台</el-button>
    <el-button @click="goToUser">去前台</el-button>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from 'vue-router';

const router = useRouter();

const goToBackground = () => {
  router.push('/background').catch((err) => {
    console.error('跳转错误:', err);
  });
};
const goToUser = () => {
  router.push('/user-home').catch((err) => {
    console.error('跳转错误:', err);
  });
};
</script>
<style lang="scss" scoped></style>
