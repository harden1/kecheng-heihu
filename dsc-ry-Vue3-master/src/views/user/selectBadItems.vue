<template>
  <div class="app">
    <!-- Table Layout for Color Grid (5 columns, 4 rows) -->
    <table class="color-table">
      <thead v-if="mainObject?.data">
        <tr>
          <th class="color-btn-top">生产批次: {{ reportJson?.batchNo }}</th>
          <th class="color-btn-top">花纹型号: {{ reportRecord?.name}}</th>
          <th class="color-btn-top">颜色: {{ reportMain?.color }}</th>
          <th class="color-btn-top">度数: {{ reportRecord?.specification}}</th>
          <th class="color-btn-top">总数不合格率: {{ reportRecord?.amount}}/{{((mainObject.data.defectiveTotal/ reportRecord?.amount)*100).toFixed(2)}}%</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[0]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[0]?.badColor }"  @click="handleClick(badSortList[0]?.badName,0)">
             {{badSortList[0]?.badName }}<br />{{mainObject.data.defect1 }}<br />{{(mainObject.data.defect1/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[1]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[1]?.badColor }"  @click="handleClick(badSortList[1]?.badName,1 )">
             {{badSortList[1]?.badName }}<br />{{mainObject.data.defect2 }} <br />{{(mainObject.data.defect2/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[2]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[2]?.badColor }"  @click="handleClick(badSortList[2]?.badName ,2)">
             {{badSortList[2]?.badName }}<br />{{mainObject.data.defect3 }}<br />{{(mainObject.data.defect3/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[3]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[3]?.badColor }"  @click="handleClick(badSortList[3]?.badName  ,3)">
             {{badSortList[3]?.badName }}<br />{{mainObject.data.defect4 }}<br />{{(mainObject.data.defect4/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[4]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[4]?.badColor }"  @click="handleClick(badSortList[4]?.badName ,4 )">
             {{badSortList[4]?.badName }}<br />{{mainObject.data.defect5}}<br />{{(mainObject.data.defect5/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[5]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[5]?.badColor }"  @click="handleClick(badSortList[5]?.badName ,5)">
             {{badSortList[5]?.badName }}<br />{{mainObject.data.defect6}}<br />{{(mainObject.data.defect6/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[6]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[6]?.badColor }"  @click="handleClick(badSortList[6]?.badName,6)">
             {{badSortList[6]?.badName  }}<br />{{mainObject.data.defect7 }}<br />{{(mainObject.data.defect7/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[7]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[7]?.badColor }"  @click="handleClick(badSortList[7]?.badName,7)">
             {{badSortList[7]?.badName }}<br />{{mainObject.data.defect8}}<br />{{(mainObject.data.defect8/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[8]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[8]?.badColor }"  @click="handleClick(badSortList[8]?.badName,8)">
             {{badSortList[8]?.badName }}<br />{{mainObject.data.defect9}}<br />{{(mainObject.data.defect9/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[9]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[9]?.badColor }"  @click="handleClick(badSortList[9]?.badName, 9)">
             {{badSortList[9]?.badName }}<br />{{mainObject.data.defect10}}<br />{{(mainObject.data.defect10/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[10]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[10]?.badColor }"  @click="handleClick(badSortList[10]?.badName,10)">
             {{badSortList[10]?.badName }}<br />{{mainObject.data.defect11 }}<br />{{(mainObject.data.defect11/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[11]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[11]?.badColor }"  @click="handleClick(badSortList[11]?.badName,11 )">
             {{badSortList[11]?.badName }}<br />{{mainObject.data.defect12 }} <br />{{(mainObject.data.defect12/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[12]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[12]?.badColor }"  @click="handleClick(badSortList[12]?.badName ,12)">
             {{badSortList[12]?.badName }}<br />{{mainObject.data.defect13 }}<br />{{(mainObject.data.defect13/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
           <el-button v-if="badSortList[13]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[13]?.badColor }"  @click="handleClick(badSortList[13]?.badName ,13)">
             {{badSortList[13]?.badName }}<br />{{mainObject.data.defect14}}<br />{{(mainObject.data.defect14/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[14]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[14]?.badColor }"  @click="handleClick(badSortList[14]?.badName ,14)">
             {{badSortList[14]?.badName }}<br />{{mainObject.data.defect15 }}<br />{{(mainObject.data.defect15/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
        </tr>
        <tr>
          <td class="color-box">
            <el-button v-if="badSortList[15]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[15]?.badColor }"  @click="handleClick(badSortList[15]?.badName ,15)">
             {{badSortList[15]?.badName }}<br />{{mainObject.data.defect16 }}<br />{{(mainObject.data.defect16/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[16]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[16]?.badColor }"  @click="handleClick(badSortList[16]?.badName ,16)">
             {{badSortList[16]?.badName }}<br />{{mainObject.data.defect17 }}<br />{{(mainObject.data.defect17/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[17]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[17]?.badColor }"  @click="handleClick(badSortList[17]?.badName ,17)">
             {{badSortList[17]?.badName }}<br />{{mainObject.data.defect18 }}<br />{{(mainObject.data.defect18/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[18]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[18]?.badColor }"  @click="handleClick(badSortList[18]?.badName ,18)">
             {{badSortList[18]?.badName }}<br />{{mainObject.data.defect19 }}<br />{{(mainObject.data.defect19/reportRecord?.amount*100).toFixed(2)}}%
            </el-button>
          </td>
          <td class="color-box">
            <el-button v-if="badSortList[19]" type="primary" class="color-btn"  :style="{ backgroundColor: badSortList[19]?.badColor }"  @click="handleClick(badSortList[19]?.badName ,19)">
             {{badSortList[19]?.badName }}<br />{{mainObject.data.defect20 }}<br />{{(mainObject.data.defect20/reportRecord?.amount*100).toFixed(2)}}%
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
import {addOrReadInspectionMain,reportBadItemOne,reportBatch} from '../../api/blackLackApi/blackLackApi'
import { no } from 'element-plus/es/locales.mjs';
const userStore = useUserStore()
const mesUserId = userStore.mesUserId
const userName = userStore.userName
const router = useRouter()
const route = useRoute()
const rawData = route.query.myData as string
const parsedData = rawData ? JSON.parse(rawData) : null
const badSortList = ref([])
const parsedData1=ref()
const reportMain= ref()
const reportJson = ref()
const reportRecord = ref()
const mainObject = ref()
onMounted(async() => {
  // 处理传入的数据
  console.log("传入的数据12", parsedData.flag)
  parsedData1.value=parsedData
  console.log("进入用户界面1231",parsedData1.value.flag,typeof(parsedData1.value.flag))
  if(parsedData1.value.flag === "-1"){
    parsedData1.value.creatBy="-1";
    console.log("进入用户界面12345",parsedData1.value)
  }
  console.log("传入的数据123", parsedData)
  //获取不良项目表，并填入各个不良项目
  //查询qrcode，看看有没有这条数据，  
  //如果有：提示已经在什么时候被谁扫码，有没有报工，
  //如果没有：新增一条主表信息，记录二维码，记录工单，记录当时的不良项目
  
  let query = {
    pageNum: 1,
    pageSize: 1000
  }
  //新建或读取一条主记录
  await addOrReadMain(parsedData)
  console.log("开始报工", (parsedData))
    reportRecord.value = parsedData;
    parsedData.mesUserId = mesUserId;
    parsedData.creatBy = userName;
    console.log("mesUserId", mesUserId)
    console.log("object", parsedData)
    mainObject.value = await addOrReadInspectionMain(parsedData);
    console.log("mainObject", mainObject)
    const rawJson =  mainObject.value.data.allDefectItems;
    const createBadItemsList = JSON.parse(rawJson);
    reportMain.value = mainObject.value.data; 
    badSortList.value = createBadItemsList.sort((a, b) => Number(a.no) - Number(b.no));
    //移除状态0的记录
    badSortList.value = badSortList.value.filter(item => item.state === "true");

    const rawJsonReport=  mainObject.value.data.apiReport;
    reportJson.value = JSON.parse(rawJsonReport);
})

//开始报工
  async function addOrReadMain(object) {
    
    // reportMain.value.creatBy = userInfo.value.username;
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
async function handleClick(color: string,No) {
  console.log(`Clicked on color: ${color}`);
  reportJson.value.stopTime="0"
   console.log(mainObject.value.data);
  const formattedString = mainObject.value.data.createTime.replace(/-/g, "/");
  const stimestamp = new Date(formattedString).getTime();
  reportJson.value.reportStartTime=stimestamp;
  reportJson.value.reportEndTime= new Date().getTime();
  let parms = {
    mainId: mainObject.value.data.id,
    no: No,
    color: color,
    reportJson: reportJson.value,
  }
  let response = await reportBadItemOne(parms);
  //重新查询这条记录并刷新
  console.log("提交数据",response.data.summary)
  mainObject.value.data=response.data.summary;

}

async function submitToApiAll(){
  //生成一条主表数据，统计所有数量
  //打开弹窗，显示一条详情，等待用户确认
  //扣除不良品，传给后端不良品数据，后端进行良品报工
  //跳转到报工表，统计不良品，展示报工记录和情况
  //回传良品报工，填入返回数据和状态
  console.log("报工所有");
  reportJson.value.stopTime="0"
  const formattedString = mainObject.value.data.createTime.replace(/-/g, "/");
  const stimestamp = new Date(formattedString).getTime();
  reportJson.value.reportStartTime=stimestamp;
  reportJson.value.reportEndTime=Date.now(); //获取当前时间戳
  let parms = {
    mainId: mainObject.value.data.id,
    no: "",
    color: "",
    reportJson: reportJson.value,
  }
  await reportBatch(parms);
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
