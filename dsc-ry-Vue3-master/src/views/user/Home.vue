<template>
  <div class="scanner-container">
    <h1>首页</h1>
    <h1>二维码 / 条码扫描</h1>

    <div class="video-container" id="reader"></div>

    <div class="controls">
      <el-button @click="startScanner" :disabled="isScannerActive">重新扫描</el-button>
      <el-button @click="stopScanner" :disabled="!isScannerActive">停止扫描</el-button>
    </div>

    <div v-if="scanResult" class="result">
      <h2>扫描结果</h2>
      <p><strong>内容:</strong> {{ scanResult }}</p>
      <el-button @click="copyToClipboard">复制结果</el-button>
    </div>

    <div v-if="scannerError" class="result" style="color: red;">
      <h2>错误</h2>
      <p>{{ scannerError }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'

const scanResult = ref(null)
const scannerError = ref(null)
const isScannerActive = ref(false)

let html5QrCode = null
const startScanner = async () => {
  try {
    scanResult.value = null
    scannerError.value = null
    isScannerActive.value = true

    if (!html5QrCode) {
      html5QrCode = new Html5Qrcode('reader')
    }

    const config = {
      fps: 10,
      qrbox: { width: 300, height: 300 }
    }

    await html5QrCode.start(
      { facingMode: 'environment' },
      config,
      (decodedText) => {
        scanResult.value = decodedText
        stopScanner()
      },
      (error) => {
        // 可选：console.warn('扫描失败:', error)
      }
    )
  } catch (err) {
    scannerError.value = err.message || '启动扫描器失败'
    isScannerActive.value = false
  }
}

const stopScanner = async () => {
  if (html5QrCode && isScannerActive.value) {
    await html5QrCode.stop()
    isScannerActive.value = false
  }
}

const copyToClipboard = async () => {
  if (!scanResult.value) return
  try {
    await navigator.clipboard.writeText(scanResult.value)
    alert('已复制到剪贴板')
  } catch (err) {
    alert('复制失败，请手动复制')
  }
}

onBeforeUnmount(() => {
  stopScanner()
})
onMounted(() => {
  startScanner() // 页面加载后自动启动扫码
})
</script>

<style scoped>
.scanner-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}
.video-container {
  width: 100%;
  height: auto;
  margin: 20px 0;
}
.controls {
  margin: 20px 0;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  text-align: left;
}
.result p {
  word-break: break-all;
  margin: 10px 0;
}
</style>
