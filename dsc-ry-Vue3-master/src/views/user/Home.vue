<template>
  <div class="app">
    <!-- Header Section -->
    <el-row class="header" type="flex" justify="space-between">
      <el-col :span="5"><span></span></el-col>
      <el-col :span="5"><span></span></el-col>
      <el-col :span="5"><span></span></el-col>
      <el-col :span="5"><span></span></el-col>
      <el-col :span="5"><span></span></el-col>
    </el-row>

    <!-- Table Layout for Color Grid (5 columns, 4 rows) -->
    <table class="color-table">
        <thead>
            <tr > 
                <th class="color-btn-top">生产批次: LOT000001</th>
                <th class="color-btn-top">花纹型号: Ggf</th>
                <th class="color-btn-top">颜色: 橙7</th>
                <th class="color-btn-top">度数: 125°</th>
                <th class="color-btn-top">总数不合格率: 10/2%</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td class="color-box">
                <el-button type="primary" class="color-btn" style="background-color: #00bfae;" @click="handleClick('融色')">
                    融色 (1) 5%
                </el-button>
                </td>
                <td class="color-box">
                <el-button type="primary" class="color-btn" style="background-color: #4c7fc2;" @click="handleClick('印偏')">
                    印偏 (1) 5%
                </el-button>
                </td>
                <td class="color-box">
                <el-button type="primary" class="color-btn" style="background-color: #8f8f8f;" @click="handleClick('赭色')">
                    赭色 (1) 5%
                </el-button>
                </td>
                <td class="color-box">
                <el-button type="primary" class="color-btn" style="background-color: #f7a700;" @click="handleClick('黄色')">
                    黄色 (1) 5%
                </el-button>
                </td>
                <td class="color-box">
                <el-button type="primary" class="color-btn" style="background-color: #b5c8c9;" @click="handleClick('绿色')">
                    绿色 (1) 5%
                </el-button>
                </td>
        </tr>
      <tr>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #c8a3c3;" @click="handleClick('粉色')">
            粉色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #d6a895;" @click="handleClick('灰色')">
            灰色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #635c2b;" @click="handleClick('棕色')">
            棕色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #95c059;" @click="handleClick('淡绿')">
            淡绿 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #8e88c4;" @click="handleClick('蓝色')">
            蓝色 (1) 5%
          </el-button>
        </td>
      </tr>
      <tr>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #bc2e92;" @click="handleClick('紫色')">
            紫色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #f8e3ba;" @click="handleClick('浅黄')">
            浅黄 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #8e8dba;" @click="handleClick('浅灰')">
            浅灰 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #96b4e6;" @click="handleClick('浅蓝')">
            浅蓝 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #946228;" @click="handleClick('浅棕')">
            浅棕 (1) 5%
          </el-button>
        </td>
      </tr>
      <tr>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #00bfae;" @click="handleClick('融色')">
            融色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #4c7fc2;" @click="handleClick('印偏')">
            印偏 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #8f8f8f;" @click="handleClick('赭色')">
            赭色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #f7a700;" @click="handleClick('黄色')">
            黄色 (1) 5%
          </el-button>
        </td>
        <td class="color-box">
          <el-button type="primary" class="color-btn" style="background-color: #b5c8c9;" @click="handleClick('绿色')">
            绿色 (1) 5%
          </el-button>
        </td>
      </tr>
        </tbody>
    </table>

    <!-- Footer Section -->
    <el-row class="footer" justify="center">
      <el-button type="warning" class="btn stop-btn">暂停</el-button>
      <el-button type="success" class="btn finish-btn">完成</el-button>
    </el-row>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { ElButton, ElRow, ElCol } from 'element-plus';

export default defineComponent({
  name: 'ColorSelectionPage',
  components: {
    ElButton,
    ElRow,
    ElCol
  },
  methods: {
    handleClick(color: string) {
      console.log(`Clicked on color: ${color}`);
      // Here you can add additional functionality for each color button click.
    }
  }
});
</script>

<style scoped>
.app {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

/* Table styles */
.color-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 20px; /* Add space between cells */
}

.color-box {
  text-align: center;
  font-weight: bold;
}

.color-btn-top{
    text-align: center;
    height: 180px;
    width: 150px;
    border: 1px solid black;
}

.color-btn {
  font-size: larger;
  width: 100%; /* Make the button fill the cell */
  height: 180px; /* Set fixed height */
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Footer styles */
.footer {
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  font-size: 16px;
}

.stop-btn {
  margin-right: 20px;
  width: 190px;
}
.finish-btn {
  margin-right: 20px;
  width: 190px;
}
</style>
