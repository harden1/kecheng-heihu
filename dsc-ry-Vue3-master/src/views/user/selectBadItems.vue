<template>
  <div class="app">
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="tabClick">
      <el-tab-pane label="不良项报工" name="first">
        <table class="color-table">
          <thead v-if="mainObject?.data">
            <tr>
              <th class="color-btn-top">生产批次: {{ reportJson?.batchNo }}</th>
              <th class="color-btn-top">花纹型号: {{ reportRecord?.name }}</th>
              <th class="color-btn-top">颜色: {{ reportMain?.color }}</th>
              <th class="color-btn-top">度数: {{ reportRecord?.specification }}</th>
              <th class="color-btn-top">
                总数<br />不合格率: {{ reportRecord?.amount }}/{{
                  ((mainObject.data.defectiveTotal / reportRecord?.amount) * 100).toFixed(2)
                }}%
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in 4" :key="row">
              <td class="color-box" v-for="col in 5" :key="(row - 1) * 5 + (col - 1)">
                <el-button
                  v-if="
                    badSortList[(row - 1) * 5 + (col - 1)] &&
                    !hiddenButtons[(row - 1) * 5 + (col - 1)]
                  "
                  type="primary"
                  class="color-btn"
                  :style="{
                    backgroundColor: badSortList[(row - 1) * 5 + (col - 1)]?.badColor,
                    color: badSortList[(row - 1) * 5 + (col - 1)]?.fontColor
                  }"
                  @click="
                    onButtonClick(
                      badSortList[(row - 1) * 5 + (col - 1)]?.badName,
                      (row - 1) * 5 + (col - 1)
                    )
                  ">
                  {{ badSortList[(row - 1) * 5 + (col - 1)]?.badName }}
                  <br />{{ mainObject.data[`defect${(row - 1) * 5 + col}`] }} <br />{{
                    (
                      (mainObject.data[`defect${(row - 1) * 5 + col}`] / reportRecord?.amount) *
                      100
                    ).toFixed(2)
                  }}%
                </el-button>

                <el-button
                  v-else-if="
                    badSortList[(row - 1) * 5 + (col - 1)] &&
                    hiddenButtons[(row - 1) * 5 + (col - 1)]
                  "
                  type="default"
                  class="color-btn"
                  disabled>
                  {{ countdowns[(row - 1) * 5 + (col - 1)] }}秒
                </el-button>
              </td>
            </tr>
          </tbody>
        </table>
      </el-tab-pane>
      <el-tab-pane label="报工记录" name="second">
        <index1 ref="index1query" :query="querySubform"></index1>
      </el-tab-pane>
    </el-tabs>
    <!-- Table Layout for Color Grid (5 columns, 4 rows) -->

    <!-- Footer Section -->
    <el-row class="footer" justify="center">
      <!-- <el-button type="warning" class="btn stop-btn">暂停</el-button>
      <el-button type="success" class="btn finish-btn">完成</el-button> -->
    </el-row>
    <div class="result-actions" style="">
      <button class="btn thirdly" @click="stop">暂停</button>
      <button class="btn fourthly" @click="tabClickBtn">{{ btnLabel }}</button>
      <button class="btn fourthly" @click="submitToApiAll">报工</button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineComponent, onUnmounted, onMounted, ref, reactive } from 'vue'
  import { ElButton, ElRow, ElCol } from 'element-plus'
  import { ElMessageBox } from 'element-plus'
  import useUserStore from '../../store/modules/user'
  import { useRoute } from 'vue-router'
  import { useRouter } from 'vue-router'
  import { listBadItem } from '../../api/badItem/badItem'
  import reportRecordListFrom from '../inspection/report/index1.vue'
  import {
    addOrReadInspectionMain,
    reportBadItemOne,
    reportBatch,
    updateStopTime
  } from '../../api/blackLackApi/blackLackApi'
  import { no } from 'element-plus/es/locales.mjs'
  import type { TabsPaneContext } from 'element-plus'
  import index1 from '../inspection/report/index1.vue'
  import { color } from 'echarts'
  const userStore = useUserStore()
  const userName = userStore.userName
  const router = useRouter()
  const route = useRoute()
  const rawData = route.query.myData as string
  const parsedData = rawData ? JSON.parse(rawData) : null
  const badSortList = ref([])
  const parsedData1 = ref()
  const reportMain = ref()
  const reportJson = ref()
  const reportRecord = ref()
  const mainObject = ref()
  const debounce = ref()
  const totalPauseTime = ref(0) // 累计暂停总时间（秒）
  const activeName = ref('first')
  const btnLabel = ref('报工记录')
  const querySubform = ref()
  const index1query = ref()
  const tabClick = (tab: TabsPaneContext, event: Event) => {
    console.log('tab', tab, 'event', event)
  }
  const tabClickBtn = () => {
    if (activeName.value === 'second') {
      activeName.value = 'first'
      btnLabel.value = '报工记录'
    } else {
      activeName.value = 'second'
      btnLabel.value = '不良项报工'
    }
    index1query.value.handleQuery()
  }
  // 禁止返回

  onUnmounted(() => {
    window.removeEventListener('popstate', forbidBack)
  })

  function forbidBack() {
    history.pushState(null, '', document.URL)
  }
  onMounted(async () => {
    history.pushState(null, '', document.URL)
    window.addEventListener('popstate', forbidBack)
    // 处理传入的数据
    console.log("传入的数据:", parsedData)
    parsedData1.value = parsedData
    // console.log("进入用户界面1231",parsedData1.value.flag,typeof(parsedData1.value.flag))
    if (parsedData1.value.flag === '-1') {
      parsedData1.value.creatBy = '-1'
      // console.log("进入用户界面12345",parsedData1.value)
    }
    // console.log("传入的数据123", parsedData)
    //获取不良项目表，并填入各个不良项目
    //查询qrcode，看看有没有这条数据，
    //如果有：提示已经在什么时候被谁扫码，有没有报工，
    //如果没有：新增一条主表信息，记录二维码，记录工单，记录当时的不良项目
    //新建或读取一条主记录
    // console.log("开始报工", (parsedData))
    reportRecord.value = parsedData
    parsedData.mesUserId = userStore.mesUserId
    parsedData.mesUserName = userStore.name
    console.log('mesUserId', userStore.mesUserId, ' ' + userName, userStore)
    console.log('userStore', userStore.name)
    console.log('object', parsedData)
    mainObject.value = await addOrReadInspectionMain(parsedData)
    console.log('mainObject', mainObject.value)
    debounce.value = mainObject.value.data.debounce
    console.log('debounce', debounce)
    totalPauseTime.value = Number(mainObject.value.data.stopTime)
    const rawJson = mainObject.value.data.allDefectItems
    // console.log('rawJson', rawJson)
    const createBadItemsList = JSON.parse(rawJson)
    reportMain.value = mainObject.value.data
    badSortList.value = createBadItemsList.sort((a, b) => Number(a.no) - Number(b.no))
    //移除状态0的记录
    badSortList.value = badSortList.value.filter((item) => item.state === 'true')

    const rawJsonReport = mainObject.value.data.apiReport
    reportJson.value = JSON.parse(rawJsonReport)
    console.log('reportJson' + reportJson.value.batchNo)
    querySubform.value = {
      workOrderCode: mainObject.value.data?.qrCode
    }
  })

  let pauseStartTime: number | null = null // 当前暂停开始时间
  async function stop() {
    // 记录暂停开始时间
    pauseStartTime = Date.now()

    ElMessageBox.confirm('暂停中...', '提示', {
      confirmButtonText: '继续',
      type: 'warning',
      closeOnClickModal: false,
      showClose: false,
      closeOnPressEscape: false,
      showCancelButton: false // 只允许点击确认
    })
      .then(async () => {
        if (pauseStartTime) {
          const now = Date.now()
          const elapsed = (now - pauseStartTime) / 1000 // 转秒
          totalPauseTime.value += elapsed
          pauseStartTime = null
          console.log(
            `本次暂停: ${elapsed.toFixed(1)} 秒，累计暂停: ${totalPauseTime.value.toFixed(1)} 秒`
          )
        }
        //更新暂停累计时间
        let parms = {
          mainId: mainObject.value.data.id,
          stopTime: totalPauseTime.value
        }
        await updateStopTime(parms)
      })
      .catch(() => {
        console.log('取消暂停')
        // 弹窗被取消或关闭（这里其实不会触发，因为只显示确认）
      })
  }
  function logout() {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logOut().then(() => {
          location.href = '/index'
        })
      })
      .catch(() => {})
  }
  const debounceMs = Number(debounce.value) * 1000

  // 记录按钮是否隐藏
  const hiddenButtons = reactive<Record<number, boolean>>({})

  // 倒计时数字
  const countdowns = reactive<Record<number, number>>({})

  function onButtonClick(name: string, index: number) {
    if (hiddenButtons[index]) return // 防抖中

    hiddenButtons[index] = true // 点击后隐藏按钮
    countdowns[index] = Number(debounce.value) // 初始化倒计时

    handleClickApi(name, index) // 调用 API

    // 每秒更新倒计时
    const timer = setInterval(() => {
      countdowns[index]--
      if (countdowns[index] <= 0) {
        clearInterval(timer)
        hiddenButtons[index] = false // 倒计时结束显示按钮
      }
    }, 1000)
  }

  import { ElMessage } from 'element-plus'
  async function handleClickApi(color: string, No) {
    console.log(`Clicked on color: ${color}`)
    reportJson.value.stopTime = '0'
    console.log(mainObject.value.data)
    const formattedString = mainObject.value.data.createTime.replace(/-/g, '/')
    const stimestamp = new Date(formattedString).getTime()
    reportJson.value.reportStartTime = stimestamp
    reportJson.value.reportEndTime = new Date().getTime()
    //判断用户id是否为空
    let response
    if (mainObject.value.data.id === '' || mainObject.value.data.id === null) {
      //提示：
      ElMessage.error('报工失败，用户ID缺失，请重新登陆，或者联系管理员')
    } else {
      let parms = {
        mainId: mainObject.value.data.id,
        no: No,
        color: color,
        reportJson: reportJson.value
      }
       response= await reportBadItemOne(parms)
    }

    //重新查询这条记录并刷新
    console.log('提交数据', response.data.summary)
    mainObject.value.data = response.data.summary
  }

  async function submitToApiAll() {
    //生成一条主表数据，统计所有数量
    //打开弹窗，显示一条详情，等待用户确认
    //扣除不良品，传给后端不良品数据，后端进行良品报工
    //跳转到报工表，统计不良品，展示报工记录和情况
    //回传良品报工，填入返回数据和状态
    console.log('报工所有')
    reportJson.value.stopTime = '0'
    const formattedString = mainObject.value.data.createTime.replace(/-/g, '/')
    const stimestamp = new Date(formattedString).getTime()
    reportJson.value.reportStartTime = stimestamp
    reportJson.value.reportEndTime = Date.now() //获取当前时间戳
    let parms = {
      mainId: mainObject.value.data.id,
      no: '',
      color: '',
      reportJson: reportJson.value
    }
    await reportBatch(parms)
    router.push({
      path: '/submitToApiUser',
      query: {
        workOrderCode: mainObject.value.data?.workOrderCode
      }
    })
  }
</script>

<style scoped></style>
<style scoped src="../../assets/styles/user.scss"></style>
