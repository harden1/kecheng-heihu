<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      label-width="100px">
      <el-form-item label="不良项名称" prop="badName">
        <el-input
          v-model="queryParams.badName"
          placeholder="请输入不良项名称"
          clearable
          @keyup.enter="handleQuery" />
      </el-form-item>
      <!-- <el-form-item label="不良项颜色" prop="badColor">
        <el-input v-model="queryParams.badColor" placeholder="请输入不良项颜色" clearable @keyup.enter="handleQuery" />
      </el-form-item> -->
      <!-- <el-form-item label="顺序" prop="no">
        <el-input v-model="queryParams.no" placeholder="请输入顺序" clearable @keyup.enter="handleQuery" />
      </el-form-item> -->
      <!-- <el-form-item label="启用状态" prop="state">
        <el-input v-model="queryParams.state" placeholder="请输入启用状态" clearable @keyup.enter="handleQuery" />
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['badItem:badItem:add']">新增</el-button>
      </el-col> -->
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['badItem:badItem:edit']"
          >修改</el-button
        >
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['badItem:badItem:remove']">删除</el-button>
      </el-col> -->
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['badItem:badItem:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="badItemList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="编号" align="center" prop="id" /> -->
      <el-table-column label="不良项名称" align="center" prop="badName" />
      <!-- <el-table-column label="不良项颜色"  align="center" prop="badColor" /> -->
      <el-table-column prop="badColor" label="不良项颜色">
        <template #default="{ row }">
          <div
            :style="{
              backgroundColor: row.badColor,
              padding: '8px',
              borderRadius: '4px'
            }">
            {{ row.badColor }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fontColor" label="字体颜色">
        <template #default="{ row }">
          <div
            :style="{
              backgroundColor: row.fontColor,
              padding: '8px',
              borderRadius: '4px'
            }">
            {{ row.fontColor }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="顺序" align="center" sortable prop="no" />
      <!-- <el-table-column label="启用状态" align="center" prop="state" /> -->
      <el-table-column
        prop="tag"
        label="启用状态"
        :filters="[
          { text: 'true', value: 'true' },
          { text: 'false', value: 'false' }
        ]"
        :filter-method="filterTag"
        filter-placement="bottom-end">
        <template #default="scope">
          <el-tag :type="scope.row.state === 'true' ? 'success' : 'error'">{{
            scope.row.state === 'true' ? '启用' : '停用'
          }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['badItem:badItem:edit']"
            >修改</el-button
          >
          <!-- <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
            v-hasPermi="['badItem:badItem:remove']">删除</el-button> -->
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改badItem对话框 -->
    <!-- <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="badItemRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="不良项名称" prop="badName">
          <el-input v-model="form.badName" placeholder="请输入不良项名称" />
        </el-form-item>
        <el-form-item label="不良项颜色" prop="badColor">
          <el-input v-model="form.badColor" placeholder="请输入不良项颜色" />
        </el-form-item>
        <el-form-item label="顺序" prop="no">
          <el-input v-model="form.no" placeholder="请输入顺序" />
        </el-form-item>
        <el-form-item label="启用状态" prop="state">
          <el-input v-model="form.state" placeholder="请输入启用状态" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog> -->

    <el-dialog v-model="open" @open="onOpen" @close="onClose" :title="title">
      <el-form ref="badItemRef" :model="form" :rules="rules" size="default" label-width="100px">
        <el-form-item label="不良项名称" prop="field102">
          <el-input
            v-model="form.badName"
            type="text"
            placeholder="请输入不良项名称"
            clearable
            :style="{ width: '100%' }"></el-input>
        </el-form-item>
        <el-form-item label="不良项颜色" prop="field101">
          <el-color-picker v-model="form.badColor" size="large"></el-color-picker>
        </el-form-item>
        <el-form-item label="字体颜色" prop="field106">
          <el-color-picker v-model="form.fontColor" size="large"></el-color-picker>
        </el-form-item>
        <el-form-item label="排序" prop="field104">
          <el-select
            v-model="form.no"
            placeholder="请选择排序"
            clearable
            :style="{ width: '100%' }">
            <el-option
              v-for="(item, index) in field104Options"
              :key="index"
              :label="item.label"
              :value="item.value"
              :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="field105">
          <el-switch v-model="form.state" active-color="#148F12"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="title" v-model="open1" width="500px" append-to-body>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BadItem">
  import {
    listBadItem,
    getBadItem,
    delBadItem,
    addBadItem,
    updateBadItem
  } from '@/api/badItem/badItem'

  const { proxy } = getCurrentInstance()

  const badItemList = ref([])
  const open = ref(false)
  const open1 = ref(false)
  const loading = ref(true)
  const showSearch = ref(true)
  const ids = ref([])
  const single = ref(true)
  const multiple = ref(true)
  const total = ref(0)
  const title = ref('')

  const data = reactive({
    queryParams: {
      pageNum: 1,
      pageSize: 20,
      badName: null,
      badColor: null,
      no: null,
      state: null
    },
    rules: {},
    form: {
      badColor: undefined,
      badName: null,
      no: undefined,
      state: true
    }
    // rules: {
    //   field102: [{
    //     required: true,
    //     message: '请输入不良项名称',
    //     trigger: 'blur'
    //   }],
    //   field104: [{
    //     required: true,
    //     message: '请选择排序',
    //     trigger: 'change'
    //   }],
    //   field101: [{
    //     required: true,
    //     message: '请选择颜色',
    //     trigger: 'change'
    //   }],
    // field105: [{
    //   required: true,
    //   message: '请选择状态',
    //   trigger: 'change'
    // }],
    // }
  })
  const field104Options = ref([
    {
      label: '1',
      value: 1
    },
    {
      label: '2',
      value: 2
    },
    {
      label: '3',
      value: 3
    },
    {
      label: '4',
      value: 4
    },
    {
      label: '5',
      value: 5
    },
    {
      label: '6',
      value: 6
    },
    {
      label: '7',
      value: 7
    },
    {
      label: '8',
      value: 8
    },
    {
      label: '9',
      value: 9
    },
    {
      label: '10',
      value: 10
    },
    {
      label: '11',
      value: 11
    },
    {
      label: '12',
      value: 12
    },
    {
      label: '13',
      value: 13
    },
    {
      label: '14',
      value: 14
    },
    {
      label: '15',
      value: 15
    },
    {
      label: '16',
      value: 16
    },
    {
      label: '17',
      value: 17
    },
    {
      label: '18',
      value: 18
    },
    {
      label: '19',
      value: 19
    },
    {
      label: '20',
      value: 20
    }
  ])
  const { queryParams, form, rules } = toRefs(data)

  /** 查询badItem列表 */
  function getList() {
    loading.value = true
    listBadItem(queryParams.value).then((response) => {
      badItemList.value = response.rows
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
      badName: null,
      badColor: null,
      no: null,
      state: null,
      createBy: null,
      createTime: null,
      updateBy: null,
      updateTime: null
    }
    proxy.resetForm('badItemRef')
  }

  /** 搜索按钮操作 */
  function handleQuery() {
    queryParams.value.pageNum = 1
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
  function heihuAPI() {
    // reset()
    // open1.value = true
    // title.value = "添加badItem"
  }
  /** 新增按钮操作 */
  function handleAdd() {
    reset()
    open.value = true
    title.value = '添加不良项'
  }
  /** 修改按钮操作 */
  function handleUpdate(row) {
    reset()
    const _id = row.id || ids.value
    getBadItem(_id).then((response) => {
      form.value = response.data
      open.value = true
      title.value = '修改不良项'
      form.value.state = true
    })
  }

  /** 提交按钮 */
  function submitForm() {
    proxy.$refs['badItemRef'].validate((valid) => {
      console.log('修改提交验证', valid)
      if (valid) {
        if (form.value.id != null) {
          updateBadItem(form.value).then((response) => {
            proxy.$modal.msgSuccess('修改成功')
            open.value = false
            getList()
          })
        } else {
          addBadItem(form.value).then((response) => {
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
      .confirm('是否确认删除不良项编号为"' + _ids + '"的数据项？')
      .then(function () {
        return delBadItem(_ids)
      })
      .then(() => {
        getList()
        proxy.$modal.msgSuccess('删除成功')
      })
      .catch(() => {})
  }

  /** 导出按钮操作 */
  function handleExport() {
    proxy.download(
      'badItem/badItem/export',
      {
        ...queryParams.value
      },
      `badItem_${new Date().getTime()}.xlsx`
    )
  }

  getList()
</script>
