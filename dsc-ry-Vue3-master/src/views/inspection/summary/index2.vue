<template>
  <div class="app" style="margin-top: 0px; height: 88%">
    <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="标识码" prop="qrCode">
          <el-input v-model="queryParams.qrCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="工单号" prop="workOrderCode">
          <el-input v-model="queryParams.workOrderCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="度数" prop="degrees">
          <el-input v-model="queryParams.degrees" placeholder="请输入" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="成功状态" prop="successFlag">
          <el-select v-model="queryParams.successFlag" placeholder="请下拉选择" clearable :style="{ width: '100px' }">
            <el-option v-for="(item, index) in options" :key="index" :label="item.label" :value="item.value"
              :disabled="item.disabled" @keyup.enter="handleQuery"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['inspection:summary:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['inspection:summary:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['inspection:summary:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['inspection:summary:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row> -->

      <el-table v-loading="loading" :data="summaryList" @selection-change="handleSelectionChange">
        <!-- <el-table-column type="selection" width="55" align="center" /> -->
        <!-- <el-table-column label="主键ID" align="center" prop="id" /> -->
        <el-table-column label="工单" align="center" prop="workOrderCode" />
        <el-table-column label="标识码" align="center" prop="qrCode" />
        <el-table-column label="总数量" align="center" prop="totalQuantity" width="60" />
        <el-table-column label="不合格总数" align="center" prop="defectiveTotal" />
        <el-table-column label="合格数" align="center" prop="qualified" width="60" />
        <el-table-column label="度数" align="center" prop="degrees" width="80" />
        <!-- <el-table-column label="不良1" align="center" prop="defect1" />
      <el-table-column label="不良2" align="center" prop="defect2" />
      <el-table-column label="不良3" align="center" prop="defect3" />
      <el-table-column label="不良4" align="center" prop="defect4" />
      <el-table-column label="不良5" align="center" prop="defect5" />
      <el-table-column label="不良6" align="center" prop="defect6" />
      <el-table-column label="不良7" align="center" prop="defect7" />
      <el-table-column label="不良8" align="center" prop="defect8" />
      <el-table-column label="不良9" align="center" prop="defect9" />
      <el-table-column label="不良10" align="center" prop="defect10" />
      <el-table-column label="不良11" align="center" prop="defect11" />
      <el-table-column label="不良12" align="center" prop="defect12" />
      <el-table-column label="不良13" align="center" prop="defect13" />
      <el-table-column label="不良14" align="center" prop="defect14" />
      <el-table-column label="不良15" align="center" prop="defect15" />
      <el-table-column label="不良16" align="center" prop="defect16" />
      <el-table-column label="不良17" align="center" prop="defect17" />
      <el-table-column label="不良18" align="center" prop="defect18" />
      <el-table-column label="不良19" align="center" prop="defect19" />
      <el-table-column label="不良20" align="center" prop="defect20" /> -->

        <!-- <el-table-column label="状态" align="center" prop="successFlag" /> -->
        <el-table-column prop="tag" label="状态" filter-placement="bottom-end" width="60">
          <template #default="scope">
            <el-tag :type="{
              1: 'success',
              2: 'danger',
              0: 'warning'
            }[scope.row.successFlag]">
              {{
                {
                  1: '成功',
                  2: '失败',
                  0: '待报工'
              }[scope.row.successFlag]
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80">
          <template #default="scope">
            <!-- <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['inspection:summary:edit']"
            >修改</el-button
          >
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['inspection:summary:remove']"
            >删除</el-button
          > -->
            <el-button link type="primary" @click="handleUpdate(scope.row)"
              v-hasPermi="['inspection:summary:edit']">重传</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />

      <!-- 添加或修改镜检统计主对话框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
        <el-form ref="summaryRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="工单号" prop="workOrderCode">
            <el-input v-model="form.workOrderCode" placeholder="请输入工单号" />
          </el-form-item>
          <el-form-item label="总数量" prop="totalQuantity">
            <el-input v-model="form.totalQuantity" placeholder="请输入总数量" />
          </el-form-item>
          <el-form-item label="不合格总数" prop="defectiveTotal">
            <el-input v-model="form.defectiveTotal" placeholder="请输入不合格总数" />
          </el-form-item>
          <el-form-item label="不良1" prop="defect1">
            <el-input v-model="form.defect1" placeholder="请输入不良1" />
          </el-form-item>
          <el-form-item label="不良2" prop="defect2">
            <el-input v-model="form.defect2" placeholder="请输入不良2" />
          </el-form-item>
          <el-form-item label="不良3" prop="defect3">
            <el-input v-model="form.defect3" placeholder="请输入不良3" />
          </el-form-item>
          <el-form-item label="不良4" prop="defect4">
            <el-input v-model="form.defect4" placeholder="请输入不良4" />
          </el-form-item>
          <el-form-item label="不良5" prop="defect5">
            <el-input v-model="form.defect5" placeholder="请输入不良5" />
          </el-form-item>
          <el-form-item label="不良6" prop="defect6">
            <el-input v-model="form.defect6" placeholder="请输入不良6" />
          </el-form-item>
          <el-form-item label="不良7" prop="defect7">
            <el-input v-model="form.defect7" placeholder="请输入不良7" />
          </el-form-item>
          <el-form-item label="不良8" prop="defect8">
            <el-input v-model="form.defect8" placeholder="请输入不良8" />
          </el-form-item>
          <el-form-item label="不良9" prop="defect9">
            <el-input v-model="form.defect9" placeholder="请输入不良9" />
          </el-form-item>
          <el-form-item label="不良10" prop="defect10">
            <el-input v-model="form.defect10" placeholder="请输入不良10" />
          </el-form-item>
          <el-form-item label="不良11" prop="defect11">
            <el-input v-model="form.defect11" placeholder="请输入不良11" />
          </el-form-item>
          <el-form-item label="不良12" prop="defect12">
            <el-input v-model="form.defect12" placeholder="请输入不良12" />
          </el-form-item>
          <el-form-item label="不良13" prop="defect13">
            <el-input v-model="form.defect13" placeholder="请输入不良13" />
          </el-form-item>
          <el-form-item label="不良14" prop="defect14">
            <el-input v-model="form.defect14" placeholder="请输入不良14" />
          </el-form-item>
          <el-form-item label="不良15" prop="defect15">
            <el-input v-model="form.defect15" placeholder="请输入不良15" />
          </el-form-item>
          <el-form-item label="不良16" prop="defect16">
            <el-input v-model="form.defect16" placeholder="请输入不良16" />
          </el-form-item>
          <el-form-item label="不良17" prop="defect17">
            <el-input v-model="form.defect17" placeholder="请输入不良17" />
          </el-form-item>
          <el-form-item label="不良18" prop="defect18">
            <el-input v-model="form.defect18" placeholder="请输入不良18" />
          </el-form-item>
          <el-form-item label="不良19" prop="defect19">
            <el-input v-model="form.defect19" placeholder="请输入不良19" />
          </el-form-item>
          <el-form-item label="不良20" prop="defect20">
            <el-input v-model="form.defect20" placeholder="请输入不良20" />
          </el-form-item>
          <el-divider content-position="center">报工记录信息</el-divider>
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button type="primary" icon="Plus" @click="handleAddInspectionReport">添加</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button type="danger" icon="Delete" @click="handleDeleteInspectionReport">删除</el-button>
            </el-col>
          </el-row>
          <el-table :data="inspectionReportList" :row-class-name="rowInspectionReportIndex"
            @selection-change="handleInspectionReportSelectionChange" ref="inspectionReport">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="序号" align="center" prop="index" width="50" />
            <el-table-column label="工单号" prop="workOrderCode" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.workOrderCode" placeholder="请输入工单号" />
              </template>
            </el-table-column>
            <el-table-column label="报工时间" prop="reportTime" width="240">
              <template #default="scope">
                <el-date-picker clearable v-model="scope.row.reportTime" type="date" value-format="YYYY-MM-DD"
                  placeholder="请选择报工时间">
                </el-date-picker>
              </template>
            </el-table-column>
            <el-table-column label="报工类型，如“自动”、“手动”等" prop="reportType" width="150">
              <template #default="scope">
                <el-select v-model="scope.row.reportType" placeholder="请选择报工类型，如“自动”、“手动”等">
                  <el-option label="请选择字典生成" value="" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="报工数量" prop="quantity" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.quantity" placeholder="请输入报工数量" />
              </template>
            </el-table-column>
            <el-table-column label="成功状态，true表示成功" prop="successFlag" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.successFlag" placeholder="请输入成功状态，true表示成功" />
              </template>
            </el-table-column>
            <el-table-column label="不良项备注" prop="defectRemark" width="150">
              <template #default="scope">
                <el-input v-model="scope.row.defectRemark" placeholder="请输入不良项备注" />
              </template>
            </el-table-column>
          </el-table>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
    <div class="result-actions" style="">
      <button class="btn thirdly" @click="toScan">继续扫码</button>
      <button class="btn primary" @click="logout">下线</button>
    </div>
  </div>
</template>

<script setup name="Summary">
import { ref, onMounted, onUnmounted } from 'vue'
import {
  listSummary,
  getSummary,
  delSummary,
  addSummary,
  updateSummary
} from '@/api/inspection/summary'

const { proxy } = getCurrentInstance()

const summaryList = ref([])
const inspectionReportList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedInspectionReport = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
import { useRouter } from 'vue-router'
const router = useRouter()
const route = useRoute()
const workOrderCode = ref()
const qrCode = ref()
const successFlag = ref()
const degrees = ref()

// 禁止返回
onMounted(() => {
  history.pushState(null, '', document.URL)
  window.addEventListener('popstate', forbidBack)
})

onUnmounted(() => {
  window.removeEventListener('popstate', forbidBack)
  handleQuery()
})
function forbidBack() {
  history.pushState(null, '', document.URL)
}
function toScan() {
  router.push({
    path: '/index'
    // query: {
    //   myData: JSON.stringify() // 如果是对象要序列化
    // }
  })
}
import { ElMessageBox, ElMessage } from 'element-plus'
import useUserStore from '../../../store/modules/user'
const userStore = useUserStore()
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
    .catch(() => { })
}
console.log('接收到的 qrCode:', workOrderCode)
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    workOrderCode: route.query.workOrderCode,
    qrCode: null, // 添加标识码
    degrees: null, // 添加度数
    successFlag: null // 添加成功状态
  },
  rules: {
    workOrderCode: [{ required: true, message: '工单号不能为空', trigger: 'blur' }],
    totalQuantity: [{ required: true, message: '总数量不能为空', trigger: 'blur' }]
  }
})
onMounted(() => {
  // 等待2s 后执行搜索
  workOrderCode.value = route.query.workOrderCode
  setTimeout(() => {
    handleQuery()
  }, 100)
})
const { queryParams, form, rules } = toRefs(data)

/** 查询镜检统计主列表 */
function getList() {
  loading.value = true
  listSummary(queryParams.value)
    .then((response) => {
      // 给每条记录增加 qualified 字段
      summaryList.value = response.rows.map((item) => ({
        ...item,
        qualified: (item.totalQuantity || 0) - (item.defectiveTotal || 0) // 合格数
      }))
      total.value = response.total
    })
    .catch((error) => {
      console.error('获取列表失败', error)
    })
    .finally(() => {
      loading.value = false
    })
}
const options = ref([
  {
    label: '成功',
    value: 1
  },
  {
    label: '待报工',
    value: 0
  },
  {
    label: '失败',
    value: 2
  }
])
// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    workOrderCode: null,
    totalQuantity: null,
    defectiveTotal: null,
    defect1: null,
    defect2: null,
    defect3: null,
    defect4: null,
    defect5: null,
    defect6: null,
    defect7: null,
    defect8: null,
    defect9: null,
    defect10: null,
    defect11: null,
    defect12: null,
    defect13: null,
    defect14: null,
    defect15: null,
    defect16: null,
    defect17: null,
    defect18: null,
    defect19: null,
    defect20: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  }
  inspectionReportList.value = []
  proxy.resetForm('summaryRef')
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  queryParams.value.workOrderCode = workOrderCode
  queryParams.value.qrCode = qrCode
  queryParams.value.degrees = degrees
  queryParams.value.successFlag = successFlag
  console.log('搜索参数:', queryParams.value) // 调试用，查看参数是否正确
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加镜检统计主'
}
import { reReportBatch } from '@/api/blackLackApi/blackLackApi'
/** 修改按钮操作 */
async function handleUpdate(row) {
  reset()
  console.log(row.successFlag)
  if (row.successFlag == 1) {
    ElMessage.error('成功的报工记录不可重复报工')
    return
  } else {
    await reReportBatch(row)
    //handleUpdate(row)
  }
  // const _id = row.id || ids.value
  // getSummary(_id).then((response) => {
  //   form.value = response.data
  //   inspectionReportList.value = response.data.inspectionReportList
  //   open.value = true
  //   title.value = '修改镜检统计主'
  // })
}

/** 提交按钮 */
function submitForm() {
  console.log('handleAdd')
  proxy.$refs['summaryRef'].validate((valid) => {
    if (valid) {
      form.value.inspectionReportList = inspectionReportList.value
      if (form.value.id != null) {
        updateSummary(form.value).then((response) => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addSummary(form.value).then((response) => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal
    .confirm('是否确认删除镜检统计主编号为"' + _ids + '"的数据项？')
    .then(function () {
      return delSummary(_ids)
    })
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => { })
}

/** 报工记录序号 */
function rowInspectionReportIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 报工记录添加按钮操作 */
function handleAddInspectionReport() {
  let obj = {}
  obj.workOrderCode = ''
  obj.reportTime = ''
  obj.reportType = ''
  obj.quantity = ''
  obj.resultJson = ''
  obj.successFlag = ''
  obj.defectRemark = ''
  inspectionReportList.value.push(obj)
}

/** 报工记录删除按钮操作 */
function handleDeleteInspectionReport() {
  if (checkedInspectionReport.value.length == 0) {
    proxy.$modal.msgError('请先选择要删除的报工记录数据')
  } else {
    const inspectionReports = inspectionReportList.value
    const checkedInspectionReports = checkedInspectionReport.value
    inspectionReportList.value = inspectionReports.filter(function (item) {
      return checkedInspectionReports.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
function handleInspectionReportSelectionChange(selection) {
  checkedInspectionReport.value = selection.map((item) => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download(
    'inspection/summary/export',
    {
      ...queryParams.value
    },
    `summary_${new Date().getTime()}.xlsx`
  )
}
getList()
handleQuery()
</script>
<style scoped src="@/assets/styles/user.scss"></style>
