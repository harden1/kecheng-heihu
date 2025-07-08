<template>
  <div>
    <el-button @click="printWithPrintJS">使用 print-js 打印</el-button>
    <div id="printMe">
      <div>第一页</div>
      <div style="page-break-after: always"></div>
      <div>第二页</div>
    </div>
  </div>
</template>

<script setup>
import printJS from "print-js";

const printWithPrintJS = () => {
  printJS({
    printable: "printMe",  // 打印区域 ID
    type: "html",          // 打印类型（HTML）
    scanStyles: false,     // 不扫描样式（可自定义样式）
    onPrintDialogClose: () => {
      console.log("打印对话框关闭");
    },
  });
};
</script>