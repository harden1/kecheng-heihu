<template>
  <div class="app">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1>二维码扫描</h1>
    </div>
    <!-- 扫描区域 -->
    <div class="scanner-area" style="margin-left: auto; margin-right: auto;">
      <div class="" id="reader"></div>
      <div style="height: 78%; ">
        <div class="scan-line" style="" v-if="isScannerActive"></div>
      </div>

    </div>
    <!-- 扫描结果 -->
    <div class="result-area">
      <h2 style=" text-align: center ;">扫描结果</h2>
      <!-- 订单详情区域 -->
      <div class="order-area">
        <table style="width: 50%;height: 100%; margin-left: auto; margin-right: auto;font-size: 16px;">
          <thead>
          </thead>
          <tbody>
            <tr>
              <td>当前用户：</td>
              <td>123</td>
            </tr>
            <tr>
              <td>工单号：</td>
              <td>11232</td>
            </tr>

            <tr>
              <td>数量：</td>
              <td>333</td>
            </tr>
            <tr>
              <td>颜色：</td>
              <td>1444</td>
            </tr>
            <tr>
              <td>任务号：</td>
              <td>{{ scanResult }}</td>
            </tr>

          </tbody>
        </table>
      </div>
      <div class="result-actions" style="">
        <button class="btn primary" @click="goToUser">开始报工</button>
        <button class="btn primary" @click="isScannerActive ? stopScanner() : startScanner()">
          {{ isScannerActive ? '停止扫描' : '开始扫描' }}
        </button>
        <button class="btn secondary" @click="logout">下线</button>
      </div>
    </div>
    <!-- 错误提示 -->
    <div class="error-message" v-if="scannerError">
      {{ scannerError }}
    </div>
    <!-- 操作按钮 -->
    <!-- <div class="action-buttons-area">
      <div class="action-buttons">
        <button class="btn primary" @click="isScannerActive ? stopScanner() : startScanner()">
          {{ isScannerActive ? '停止扫描' : '开始扫描' }}
        </button>
        <button class="btn primary" @click="toggleFlash">
          {{ isScannerActive ? '关闭闪光灯' : '打开闪光灯' }}
        </button>
        <button class="btn secondary" @click="toggleFlash" v-if="hasFlash">
        {{ flashOn ? '关闭闪光灯' : '打开闪光灯' }}
      </button>
      </div>
    </div> -->
    <!-- 底部导航 -->
    <div class="footer">
      <button class="nav-btn" @click="goToBackground">
        <span class="icon-setting"></span>
        <span>后台</span>
      </button>
      <button class="nav-btn active" @click="goToSettings">
        <span class="icon-scan"></span>
        <span>设置</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '../store/modules/user'
import { queryScanTaskResult } from '../api/blackLackApi/blackLackApi'
import { checkUserToBlackLack } from "../api/system/user"
const userStore = useUserStore()
const router = useRouter()
function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => { })
}


// 扫描相关状态
const scanResult = ref(null)
const scannerError = ref(null)
const isScannerActive = ref(false)
const hasFlash = ref(false)
const flashOn = ref(false)
let html5QrCode = null
const resData = ref(null)
// 启动扫描（强制后置摄像头）
const startScanner = async () => {
  const res = await userStore.getInfo()
  const username = res.user.userName
  console.log("启动扫描", username.username, username)
  checkUserToBlackLack(username).then(res => {
    console.log("查询用户接口返回数据", res)
    if (res.code === 200 && res.msg === "0") {
      // 清空token和用户信息
      userStore.logOut().then(() => {
        ElMessageBox.confirm(
          '在MES中没有找到此用户，请联系管理员！',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).finally(() => {
          location.href = '/index'
        })
      })
    }
  })
  try {
    scanResult.value = null;
    scannerError.value = null;
    isScannerActive.value = true;

    if (!html5QrCode) {
      html5QrCode = new Html5Qrcode('reader');
    }

    const config = {
      fps: 10,
      qrbox: { width: 200, height: 200 }
    };

    await html5QrCode.start(
      { facingMode: "environment" },
      config,
      (decodedText) => {
        // 成功扫描到二维码
        scanResult.value = decodedText;
        stopScanner();
        scanResQueryApi(decodedText)
      },
      (error) => {
        // 关键修改：忽略"未检测到二维码"的常规错误
        if (!error.message.includes('No barcode or QR code')) {
          // 只处理其他真实错误（如摄像头权限问题）
          console.error('扫描异常:', error);
          scannerError.value = '扫描器异常';
          stopScanner();
        }
        // 否则静默继续扫描
      }
    );
  } catch (err) {
    // 处理启动失败（如无摄像头权限）
    scannerError.value = err.message || '启动扫描器失败';
    isScannerActive.value = false;
  }
};

async function scanResQueryApi(taskCode) {
  console.log("taskCode", taskCode);
  //调用后端接口查询
  let response = await queryScanTaskResult(taskCode);
  console.log("扫描结果", response.data);
  resData.value = response.data;
  //显示在扫描结果表格
  //准备进入不良品报工界面
};
// 路由跳转
const goToBackground = () => {
  //验证权限是否是管理员
  if (userStore.roles.includes('admin')) {
    router.push('/background')
  } else {
    ElMessage.error('您没有权限进入后台管理界面');
    return;
  }
}
const goToSettings = () => {
  //验证权限是否是管理员
  if (userStore.roles.includes('admin')) {
    router.push('/selectBadItems')
  } else {
    ElMessage.error('您没有权限进入设置界面');
    return;
  }
}

const goToUser = () => {
  try {
    console.log("进入用户界面", resData.value.taskCode, scanResult.value)
  } catch (error) {
    ElMessage.error('扫描失败，请重新扫描');
    return;
  }
  if (resData.value === null || resData.value.taskCode !== scanResult.value) {
    return;
  }
  router.push({
    path: '/selectBadItems',
    query: {
      myData: JSON.stringify(resData.value) // 如果是对象要序列化
    }
  })
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

<style scoped src="../assets/styles/user.scss"></style>