<template>
  <div class="app" style="margin-top: 10px;">
  <div class="app-container">
      <el-form style="margin-left: 25px; margin-bottom: 10px;" :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <!-- <el-form-item label="主表ID" prop="summaryId">
          <el-input
            v-model="queryParams.summaryId"
            placeholder="请输入主表ID"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->
        <!-- <el-form-item label="工单号" prop="workOrderCode">
          <el-input
            v-model="queryParams.workOrderCode"
            placeholder="请输入工单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->
        <!-- <el-form-item label="报工时间" prop="reportTime">
          <el-date-picker clearable
            v-model="queryParams.reportTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择报工时间">
          </el-date-picker>
        </el-form-item> -->
        <!-- <el-form-item label="报工数量" prop="quantity">
          <el-input
            v-model="queryParams.quantity"
            placeholder="请输入报工数量"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->
        <el-form-item label="成功状态" prop="successFlag">
          <!-- <el-input
            v-model="queryParams.successFlag"
            placeholder="请输入成功状态，true表示成功"
            clearable
            @keyup.enter="handleQuery"
          /> -->
          <el-select v-model="queryParams.successFlag" placeholder="请选择下拉选择" clearable :style="{width: '100px'}">
            <el-option v-for="(item, index) in options" :key="index" :label="item.label"
              :value="item.value" :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="不良项备注" prop="defectRemark">
          <el-input
            v-model="queryParams.defectRemark"
            placeholder="请输入不良项备注"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['inspection:report:add']"
          >新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['inspection:report:edit']"
          >修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['inspection:report:remove']"
          >删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['inspection:report:export']"
          >导出</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row> -->

      <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
        <!-- <el-table-column type="selection" width="55" align="center" /> -->
        <!-- <el-table-column label="主键ID" align="center" prop="id" />
        <el-table-column label="主表ID" align="center" prop="summaryId" /> -->
        <el-table-column label="工单号" align="center" prop="workOrderCode" />
        <el-table-column label="报工时间" align="center" prop="reportTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.reportTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <!-- <el-table-column label="报工类型" align="center" prop="reportType" />
        <el-table-column label="报工数量" align="center" prop="quantity" />
        <el-table-column label="返回json内容" align="center" prop="resultJson" /> -->
        <el-table-column label="成功状态" align="center" prop="successFlag" />
        <el-table-column label="不良项" align="center" prop="defectRemark" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link style="color: #02B980; text-decoration: underline;"  @click="handleUpdate(scope.row)" v-hasPermi="['inspection:report:edit']">重新提交</el-button>
            <!-- <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['inspection:report:remove']">删除</el-button> -->
          </template>
        </el-table-column>
      </el-table>
      
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <!-- 添加或修改报工记录对话框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
        <el-form ref="reportRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="主表ID" prop="summaryId">
            <el-input v-model="form.summaryId" placeholder="请输入主表ID" />
          </el-form-item>
          <el-form-item label="工单号" prop="workOrderCode">
            <el-input v-model="form.workOrderCode" placeholder="请输入工单号" />
          </el-form-item>
          <el-form-item label="报工时间" prop="reportTime">
            <el-date-picker clearable
              v-model="form.reportTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择报工时间">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="报工数量" prop="quantity">
            <el-input v-model="form.quantity" placeholder="请输入报工数量" />
          </el-form-item>
          <el-form-item label="返回json内容" prop="resultJson">
            <el-input v-model="form.resultJson" type="textarea" placeholder="请输入内容" />
          </el-form-item>
          <el-form-item label="成功状态，true表示成功" prop="successFlag">
            <el-input v-model="form.successFlag" placeholder="请输入成功状态，true表示成功" />
          </el-form-item>
          <el-form-item label="不良项备注" prop="defectRemark">
            <el-input v-model="form.defectRemark" placeholder="请输入不良项备注" />
          </el-form-item>
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

<script setup name="Report">
import { listReport, getReport, delReport, addReport, updateReport } from "@/api/inspection/report"
import { ElMessageBox } from 'element-plus'
import useUserStore from '../../../store/modules/user'
const userStore = useUserStore()
const { proxy } = getCurrentInstance()
const reportList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
import { useRouter } from 'vue-router'
const router = useRouter()

const options = ref([{
  "label": "成功",
  "value": 1
}, {
  "label": "失败",
  "value": 2
}])
const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    summaryId: null,
    workOrderCode: null,
    reportTime: null,
    reportType: null,
    quantity: null,
    resultJson: null,
    successFlag: null,
    defectRemark: null,
  },
  rules: {
    summaryId: [
      { required: true, message: "主表ID不能为空", trigger: "blur" }
    ],
    workOrderCode: [
      { required: true, message: "工单号不能为空", trigger: "blur" }
    ],
    reportTime: [
      { required: true, message: "报工时间不能为空", trigger: "blur" }
    ],
    quantity: [
      { required: true, message: "报工数量不能为空", trigger: "blur" }
    ],
  }
})
function toScan(){
  router.push({
    path: '/index',
    // query: {
    //   myData: JSON.stringify() // 如果是对象要序列化
    // }
  })
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
const { queryParams, form, rules } = toRefs(data)

/** 查询报工记录列表 */
function getList() {
  loading.value = true
  listReport(queryParams.value).then(response => {
    reportList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    summaryId: null,
    workOrderCode: null,
    reportTime: null,
    reportType: null,
    quantity: null,
    resultJson: null,
    successFlag: null,
    defectRemark: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  }
  proxy.resetForm("reportRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加报工记录"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getReport(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改报工记录"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["reportRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateReport(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReport(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
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
  proxy.$modal.confirm('是否确认删除报工记录编号为"' + _ids + '"的数据项？').then(function() {
    return delReport(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('inspection/report/export', {
    ...queryParams.value
  }, `report_${new Date().getTime()}.xlsx`)
}

getList()
</script>
<style scoped src="../../../assets/styles/user.scss"></style>
