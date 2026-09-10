<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="二维码" prop="qrCode">
        <el-input v-model="queryParams.qrCode" placeholder="请输入二维码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工单号" prop="workOrderCode">
        <el-input v-model="queryParams.workOrderCode" placeholder="请输入工单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="任务ID" prop="taskId">
        <el-input v-model="queryParams.taskId" placeholder="请输入任务ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="上传状态" prop="uploadStatus">
        <el-select v-model="queryParams.uploadStatus" placeholder="请选择" clearable style="width: 120px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-"
          start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="RefreshRight" :disabled="multiple" @click="handleRetry"
          v-hasPermi="['inspection:feedRecord:retry']">重新上传</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="feedRecordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" align="center" prop="id" width="80" />
      <el-table-column label="二维码" align="center" prop="qrCode" width="180">
        <template #default="scope">
          <span class="wrap-cell">{{ scope.row.qrCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="工单号" align="center" prop="workOrderCode" width="150">
        <template #default="scope">
          <span class="wrap-cell">{{ scope.row.workOrderCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务ID" align="center" prop="taskId" width="120" />
      <el-table-column label="原料ID" align="center" prop="materialId" width="120" />
      <el-table-column label="投料数量" align="center" prop="feedAmount" width="100" />
      <el-table-column label="上传状态" align="center" prop="uploadStatus" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.uploadStatus)">
            {{ statusText(scope.row.uploadStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="重传次数" align="center" prop="retryCount" width="80" />
      <el-table-column label="失败类型" align="center" prop="failType" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.failType" :type="failTypeTagType(scope.row.failType)">
            {{ failTypeText(scope.row.failType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="失败原因" align="center" prop="errorMessage" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="165">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="成功时间" align="center" prop="feedTime" width="165">
        <template #default="scope">
          <span>{{ parseTime(scope.row.feedTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="canRetry(scope.row.uploadStatus)" link type="warning" icon="RefreshRight"
            @click="handleRetryOne(scope.row)">重新上传</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="投料记录详情" size="50%">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="记录ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="镜检主记录ID">{{ detailData.summaryId }}</el-descriptions-item>
        <el-descriptions-item label="二维码">{{ detailData.qrCode }}</el-descriptions-item>
        <el-descriptions-item label="工单号">{{ detailData.workOrderCode }}</el-descriptions-item>
        <el-descriptions-item label="任务ID">{{ detailData.taskId }}</el-descriptions-item>
        <el-descriptions-item label="原料物料ID">{{ detailData.materialId }}</el-descriptions-item>
        <el-descriptions-item label="库存明细ID">{{ detailData.inventoryElementId }}</el-descriptions-item>
        <el-descriptions-item label="投料数量">{{ detailData.feedAmount }}</el-descriptions-item>
        <el-descriptions-item label="单位ID">{{ detailData.unitId }}</el-descriptions-item>
        <el-descriptions-item label="上传状态">
          <el-tag :type="statusTagType(detailData.uploadStatus)">{{ statusText(detailData.uploadStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重传次数">{{ detailData.retryCount }}</el-descriptions-item>
        <el-descriptions-item label="失败类型">
          <el-tag v-if="detailData.failType" :type="failTypeTagType(detailData.failType)">
            {{ failTypeText(detailData.failType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="失败原因">{{ detailData.errorMessage }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="成功时间">{{ parseTime(detailData.feedTime) }}</el-descriptions-item>
        <el-descriptions-item label="请求JSON">
          <pre class="json-preview">{{ formatJson(detailData.requestJson) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应JSON">
          <pre class="json-preview">{{ formatJson(detailData.responseJson) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="FeedRecord">
import { listFeedRecord, getFeedRecord, retryFeedRecord } from '@/api/inspection/feedRecord'

const { proxy } = getCurrentInstance()

const feedRecordList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const detailData = ref({})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  qrCode: undefined,
  workOrderCode: undefined,
  taskId: undefined,
  uploadStatus: undefined
})

/** 状态选项 */
const statusOptions = [
  { label: '待上传', value: 'PENDING' },
  { label: '上传中', value: 'PROCESSING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILED' },
  { label: '结果未知', value: 'UNKNOWN' }
]

/** 状态文案映射 */
function statusText(status) {
  const map = { PENDING: '待上传', PROCESSING: '上传中', SUCCESS: '成功', FAILED: '失败', UNKNOWN: '结果未知' }
  return map[status] || status
}

/** 状态标签类型映射 */
function statusTagType(status) {
  const map = { PENDING: 'warning', PROCESSING: 'info', SUCCESS: 'success', FAILED: 'danger', UNKNOWN: 'info' }
  return map[status] || 'info'
}

/** 失败类型文案映射 */
function failTypeText(type) {
  const map = { FEED_RELATION: '投料关系无效', INVENTORY: '库存查询无效', UPLOAD: '上传失败' }
  return map[type] || type
}

/** 失败类型标签类型映射 */
function failTypeTagType(type) {
  const map = { FEED_RELATION: 'warning', INVENTORY: 'warning', UPLOAD: 'danger' }
  return map[type] || 'info'
}

/** 是否可重传：仅 PENDING/FAILED 显示重传按钮 */
function canRetry(status) {
  return status === 'PENDING' || status === 'FAILED'
}

/** 格式化JSON */
function formatJson(str) {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

/** 查询列表 */
function getList() {
  loading.value = true
  listFeedRecord(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    feedRecordList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 详情 */
function handleDetail(row) {
  getFeedRecord(row.id).then(response => {
    detailData.value = response.data
    detailVisible.value = true
  })
}

/** 单条重传 */
function handleRetryOne(row) {
  proxy.$modal.confirm('确认重新上传该投料记录？').then(() => {
    return retryFeedRecord(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('重传成功')
  }).catch(() => {})
}

/** 批量重传 */
function handleRetry() {
  if (ids.value.length === 0) return
  proxy.$modal.confirm('确认重新上传选中的' + ids.value.length + '条投料记录？').then(() => {
    const promises = ids.value.map(id => retryFeedRecord(id))
    Promise.all(promises).then(() => {
      getList()
      proxy.$modal.msgSuccess('重传完成')
    })
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.wrap-cell {
  display: inline-block;
  max-width: 100%;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-all;
  line-height: 1.4;
}

.json-preview {
  max-height: 300px;
  overflow: auto;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
