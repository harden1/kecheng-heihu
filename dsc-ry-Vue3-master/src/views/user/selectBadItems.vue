<template>
  <div class="app">
    <!-- Table Layout for Color Grid (5 columns, 4 rows) -->
    <table class="color-table">
      <thead>
        <tr>
          <th class="color-btn-top">生产批次: {{ reportJson?.batchNo }}</th>
          <th class="color-btn-top">花纹型号: {{ reportRecord?.name}}</th>
          <th class="color-btn-top">颜色: {{ reportMain?.color }}</th>
          <th class="color-btn-top">度数: {{ reportRecord?.specification}}</th>
          <th class="color-btn-top">总数不合格率: {{ reportRecord?.amount}}/</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[0]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[0]?.badColor }"  @click="handleClick(badSortList[0]?.badName )">
             {{badSortList[0]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[1]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[1]?.badColor }"  @click="handleClick(badSortList[1]?.badName )">
             {{badSortList[1]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[2]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[2]?.badColor }"  @click="handleClick(badSortList[2]?.badName )">
             {{badSortList[2]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[3]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[3]?.badColor }"  @click="handleClick(badSortList[3]?.badName )">
             {{badSortList[3]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[4]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[4]?.badColor }"  @click="handleClick(badSortList[4]?.badName )">
             {{badSortList[4]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[5]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[5]?.badColor }"  @click="handleClick(badSortList[5]?.badName )">
             {{badSortList[5]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[6]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[6]?.badColor }"  @click="handleClick(badSortList[6]?.badName )">
             {{badSortList[6]?.badName  }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[7]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[7]?.badColor }"  @click="handleClick(badSortList[7]?.badName )">
             {{badSortList[7]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[8]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[8]?.badColor }"  @click="handleClick(badSortList[8]?.badName )">
             {{badSortList[8]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[9]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[9]?.badColor }"  @click="handleClick(badSortList[9]?.badName )">
             {{badSortList[9]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[10]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[10]?.badColor }"  @click="handleClick(badSortList[10]?.badName )">
             {{badSortList[10]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[11]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[11]?.badColor }"  @click="handleClick(badSortList[11]?.badName )">
             {{badSortList[11]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[12]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[12]?.badColor }"  @click="handleClick(badSortList[12]?.badName )">
             {{badSortList[12]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[13]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[13]?.badColor }"  @click="handleClick(badSortList[13]?.badName )">
             {{badSortList[13]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[14]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[14]?.badColor }"  @click="handleClick(badSortList[14]?.badName )">
             {{badSortList[14]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[15]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[15]?.badColor }"  @click="handleClick(badSortList[15]?.badName )">
             {{badSortList[15]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[16]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[16]?.badColor }"  @click="handleClick(badSortList[16]?.badName )">
             {{badSortList[16]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[17]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[17]?.badColor }"  @click="handleClick(badSortList[17]?.badName )">
             {{badSortList[17]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[18]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[18]?.badColor }"  @click="handleClick(badSortList[18]?.badName )">
             {{badSortList[18]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[19]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[19]?.badColor }"  @click="handleClick(badSortList[19]?.badName )">
             {{badSortList[19]?.badName }}<br />(1) <br />5%
            </el-button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Footer Section -->
    <el-row class="footer" justify="center">
      <!-- <el-button type="warning" class="btn stop-btn">暂停</el-button>
      <el-button type="success" class="btn finish-btn">完成</el-button> -->


    </el-row>
    <div class="result-actions" style="">
      <button class="btn thirdly" @click="stop">暂停</button>
      <button class="btn fourthly" @click="submitToApiAll">报工</button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { defineComponent, onMounted ,ref} from 'vue';
import { ElButton, ElRow, ElCol } from 'element-plus';
import { ElMessageBox } from 'element-plus'
import useUserStore from '../../store/modules/user'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { listBadItem } from '../../api/badItem/badItem'
import {addOrReadInspectionMain,reportBadItemOne} from '../../api/blackLackApi/blackLackApi'
const userStore = useUserStore()
const mesUserId = userStore.mesUserId
const userName = userStore.userName
const router = useRouter()
const route = useRoute()
const rawData = route.query.myData as string
const parsedData = rawData ? JSON.parse(rawData) : null
const badSortList = ref([])
onMounted(async() => {
  // 处理传入的数据
  console.log("传入的数据", parsedData)
  //获取不良项目表，并填入各个不良项目
  //查询qrcode，看看有没有这条数据，  
  //如果有：提示已经在什么时候被谁扫码，有没有报工，
  //如果没有：新增一条主表信息，记录二维码，记录工单，记录当时的不良项目
  
  let query = {
    pageNum: 1,
    pageSize: 1000
  }
  //新建或读取一条主记录
  addOrReadMain(parsedData)
  // await listBadItem(query).then(res => {
  //   console.log("获取的不良项目列表", res)
  //   badSortList.value = res.rows.sort((a, b) => Number(a.no) - Number(b.no));
  //   badSortList.value .forEach(item => {
  //   //  console.log("item", item)
  //   })
  // })
  // 新建一条主表信息
})
const reportMain= ref(null)
const reportJson= ref(null)
const reportRecord = ref(null)
//开始报工
  async function addOrReadMain(object) {
    console.log("开始报工", (object))
    reportRecord.value = object;
    object.mesUserId = mesUserId;
    object.creatBy = userName;
    console.log("mesUserId", mesUserId)
    console.log("object", object)
    let mainObject = await addOrReadInspectionMain(object);
    console.log("mainObject", mainObject)
    const rawJson =  mainObject.data.allDefectItems;
    const createBadItemsList = JSON.parse(rawJson);
    reportMain.value = mainObject.data; 
    badSortList.value = createBadItemsList.sort((a, b) => Number(a.no) - Number(b.no));
    //移除状态0的记录
    badSortList.value = badSortList.value.filter(item => item.state === "true");

    const rawJsonReport=  mainObject.data.apiReport;
    reportJson.value = JSON.parse(rawJsonReport);
  
  };
function stop() {
  ElMessageBox.confirm('暂停中...', '提示', {
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => { })
}
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
async function handleClick(color: string) {
  console.log(`Clicked on color: ${color}`);
  let parms = {
    color: color,
    reportJson: reportJson.value,
  }
  await reportBadItemOne(parms);
}

function submitToApiAll(){
  //生成一条主表数据，统计所有数量
  //打开弹窗，显示一条详情，等待用户确认
  //扣除不良品，传给后端不良品数据，后端进行良品报工
  //跳转到报工表，统计不良品，展示报工记录和情况
  //回传良品报工，填入返回数据和状态
  console.log("报工所有");
  
  router.push({
    path: '/submitToApi',
    // query: {
    //   myData: JSON.stringify() // 如果是对象要序列化
    // }
  })
}
</script>

<style scoped></style>
<style scoped src="../../assets/styles/user.scss"></style>
