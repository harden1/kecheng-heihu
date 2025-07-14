<template>
  <div class="h5-container" style="">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1>二维码扫描</h1>
    </div>
    <!-- 扫描区域 -->
    <div class="scanner-area" style="">
      <div class="" id="reader"></div>
      <div class="scan-line" style="" v-if="isScannerActive"></div>
    </div>

    <!-- 操作按钮 -->
     <div class="action-buttons-area">
      <div class="action-buttons" >
      <button class="btn primary" @click="isScannerActive ? stopScanner() : startScanner()">
        {{ isScannerActive ? '停止扫描' : '开始扫描' }}
      </button>
      <button class="btn secondary" @click="toggleFlash" v-if="hasFlash">
        {{ flashOn ? '关闭闪光灯' : '打开闪光灯' }}
      </button>
    </div>
     </div>
    

    <!-- 扫描结果 -->
    <div class="result-area" v-if="scanResult">
      <h2>扫描结果</h2>
      <div class="result-content">
        <p>{{ scanResult }}</p>
      </div>
      <div class="result-actions">
        <button class="btn primary" @click="copyToClipboard">复制</button>
        <button class="btn secondary" @click="openResult">打开</button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div class="error-message" v-if="scannerError">
      {{ scannerError }}
    </div>

    <!-- 底部导航 -->
    <div class="footer">
      <button class="nav-btn" @click="goToBackground">
        <i class="icon-setting"></i>
        <span>后台</span>
      </button>
      <button class="nav-btn active" @click="goToUser">
        <i class="icon-scan"></i>
        <span>扫描</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'
import { useRouter } from 'vue-router'

const router = useRouter()

// 路由跳转
const goToBackground = () => router.push('/background')
const goToUser = () => router.push('/user-home')

// 扫描相关状态
const scanResult = ref(null)
const scannerError = ref(null)
const isScannerActive = ref(false)
const hasFlash = ref(false)
const flashOn = ref(false)
let html5QrCode = null

// 启动扫描
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

// 停止扫描
const stopScanner = async () => {
  if (html5QrCode && isScannerActive.value) {
    try {
      await html5QrCode.stop()
      isScannerActive.value = false
      flashOn.value = false
    } catch (err) {
      console.error('停止扫描失败:', err)
    }
  }
}

// 切换闪光灯
const toggleFlash = async () => {
  if (!html5QrCode || !isScannerActive.value) return

  try {
    if (flashOn.value) {
      await html5QrCode.turnOffFlash()
    } else {
      await html5QrCode.turnOnFlash()
    }
    flashOn.value = !flashOn.value
  } catch (err) {
    scannerError.value = '闪光灯控制失败'
  }
}

// 复制结果
const copyToClipboard = async () => {
  if (!scanResult.value) return
  try {
    await navigator.clipboard.writeText(scanResult.value)
    alert('已复制到剪贴板')
  } catch (err) {
    // 兼容性处理
    const textarea = document.createElement('textarea')
    textarea.value = scanResult.value
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    alert('已复制到剪贴板')
  }
}

// 打开扫描结果
const openResult = () => {
  if (!scanResult.value) return

  // 如果是URL，直接打开
  if (/^https?:\/\//.test(scanResult.value)) {
    window.open(scanResult.value, '_blank')
  } else {
    // 其他类型内容可以在这里添加处理逻辑
    alert(scanResult.value)
  }
}

// 生命周期
onMounted(() => {
  startScanner()
})

onBeforeUnmount(() => {
  stopScanner()
})
</script>

<style scoped>
/* 基础样式 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.h5-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background: #f5f5f5;
}

/* 头部样式 */
.header {
  padding: 15px;
  background: #02B980;
  color: white;
  text-align: center;
  box-shadow: 0 2px 5px #02B980(0, 0, 0, 0.1);
}

.header h1 {
  font-size: 1.2rem;
  font-weight: 500;
}

/* 扫描区域 */
.scanner-area {
  width: 100%;
  height: 55%;
}

.video-container {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #02B980;
  box-shadow: 0 0 10px #006847;
  animation: scan 3s linear infinite;
  
}

@keyframes scan {
  0% {
    top: 0;
  }

  100% {
    top: 100%;
  }
}

/* 按钮样式 */
.action-buttons-area { 
  height: 40%;
}
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding: 15px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn.primary {
  background: #02B980;
  color: white;
}

.btn.secondary {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.btn:active {
  transform: scale(0.98);
}

/* 结果区域 */
.result-area {
  margin: 0 15px 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.result-content {
  margin: 10px 0;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  word-break: break-all;
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.result-actions .btn {
  flex: 1;
}

/* 错误提示 */
.error-message {
  margin: 0 15px 15px;
  padding: 15px;
  background: #ffebee;
  color: #d32f2f;
  border-radius: 8px;
  text-align: center;
}

/* 底部导航 */
.footer {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
  background: white;
  border-top: 1px solid #eee;
}

.nav-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  background: none;
  border: none;
  color: #666;
  font-size: 0.8rem;
}

.nav-btn.active {
  color: #006847;
}

.nav-btn i {
  font-size: 1.2rem;
  margin-bottom: 4px;
}

/* 图标 (可以使用实际图标库) */
.icon-setting::before {
  content: "⚙️";
}

.icon-scan::before {
  content: "📷";
}
</style>