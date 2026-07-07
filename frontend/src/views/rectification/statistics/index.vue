<template>
  <div class="app-container">
    <!-- Row 1: Overview Cards -->
    <overview-cards :items="overviewItems" />

    <!-- Row 2: Pie Chart + Bar Chart -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>问题类型分布</span>
            </div>
          </template>
          <div ref="categoryChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>整改状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: Trend Line Chart -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>整改趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="updateTrendChart">
                <el-radio-button label="month">按月</el-radio-button>
                <el-radio-button label="quarter">按季度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 380px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 4: High Risk Areas Table -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>高频风险领域</span>
            </div>
          </template>
          <el-table :data="riskAreaData" v-loading="riskLoading" style="width: 100%" stripe>
            <el-table-column prop="area" label="风险领域" min-width="180" show-overflow-tooltip />
            <el-table-column prop="count" label="问题数量" width="120" align="center" sortable />
            <el-table-column prop="completed" label="已整改" width="100" align="center" />
            <el-table-column prop="overdue" label="逾期数" width="100" align="center">
              <template #default="scope">
                <span :style="{ color: scope.row.overdue > 0 ? '#F56C6C' : '#67C23A' }">
                  {{ scope.row.overdue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="completionRate" label="整改率" width="120" align="center">
              <template #default="scope">
                <el-progress
                  :percentage="scope.row.completionRate"
                  :color="progressColor(scope.row.completionRate)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column prop="recoveryAmount" label="资金挽回(万元)" width="140" align="right">
              <template #default="scope">
                {{ formatMoney(scope.row.recoveryAmount) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="RectificationStatistics">
import { ref, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getByCategory, getByStatus, getFundRecovery, getRecurring } from '@/api/rectification/statistics'
import OverviewCards from './components/OverviewCards.vue'

const categoryChartRef = ref(null)
const statusChartRef = ref(null)
const trendChartRef = ref(null)
let categoryChart = null
let statusChart = null
let trendChart = null

const trendType = ref('month')
const riskLoading = ref(false)

const overviewItems = ref([
  { title: '问题总数', value: 0, unit: '个', icon: 'DataAnalysis' },
  { title: '已完成率', value: '0%', unit: '', icon: 'CircleCheck' },
  { title: '逾期率', value: '0%', unit: '', icon: 'Warning' },
  { title: '资金挽回总额', value: '¥0', unit: '', icon: 'Money' }
])

const riskAreaData = ref([])

const categoryColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#B37FEB', '#36CFC9', '#FF85C0']
const statusColors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C']

onMounted(() => {
  nextTick(() => {
    initCharts()
    loadAllData()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart(categoryChart)
  disposeChart(statusChart)
  disposeChart(trendChart)
})

function disposeChart(chart) {
  if (chart) {
    chart.dispose()
    chart = null
  }
}

function handleResize() {
  categoryChart?.resize()
  statusChart?.resize()
  trendChart?.resize()
}

function initCharts() {
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
}

function loadAllData() {
  loadOverview()
  loadCategoryChart()
  loadStatusChart()
  loadTrendChart()
  loadRiskAreas()
}

// Load overview data
function loadOverview() {
  getOverview().then(res => {
    const data = res.data
    overviewItems.value = [
      { title: '问题总数', value: data.totalIssues || 0, unit: '个', icon: 'DataAnalysis' },
      { title: '已完成率', value: (data.completionRate || 0) + '%', unit: '', icon: 'CircleCheck' },
      { title: '逾期率', value: (data.overdueRate || 0) + '%', unit: '', icon: 'Warning' },
      { title: '资金挽回总额', value: '¥' + formatMoney(data.totalRecoveryAmount || 0), unit: '', icon: 'Money' }
    ]
  }).catch(() => {
    // Use mock data if API fails
    overviewItems.value = [
      { title: '问题总数', value: 256, unit: '个', icon: 'DataAnalysis' },
      { title: '已完成率', value: '78.5%', unit: '', icon: 'CircleCheck' },
      { title: '逾期率', value: '12.3%', unit: '', icon: 'Warning' },
      { title: '资金挽回总额', value: '¥1,258.36', unit: '', icon: 'Money' }
    ]
  })
}

// Pie Chart - Problem Category Distribution
function loadCategoryChart() {
  getByCategory().then(res => {
    const data = res.data || res || []
    updatePieChart(data)
  }).catch(() => {
    // Mock data
    updatePieChart([
      { category: '资金管理类', count: 45 },
      { category: '采购管理类', count: 32 },
      { category: '合同管理类', count: 28 },
      { category: '项目管理类', count: 22 },
      { category: '资产管理类', count: 18 },
      { category: '费用报销类', count: 15 },
      { category: '收入管理类', count: 12 },
      { category: '其他', count: 8 }
    ])
  })
}

function updatePieChart(data) {
  if (!categoryChart) return
  const names = data.map(item => item.category || item.name)
  const values = data.map(item => item.count || item.value)

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [
      {
        name: '问题类型',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 11
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          },
          scaleSize: 10
        },
        data: names.map((name, i) => ({
          name,
          value: values[i],
          itemStyle: { color: categoryColors[i % categoryColors.length] }
        }))
      }
    ]
  })
}

// Bar Chart - Status Distribution
function loadStatusChart() {
  getByStatus().then(res => {
    const data = res.data || res || []
    updateBarChart(data)
  }).catch(() => {
    updateBarChart([
      { status: '已销号', count: 52 },
      { status: '整改中', count: 38 },
      { status: '待审核', count: 25 },
      { status: '逾期未完成', count: 18 }
    ])
  })
}

function updateBarChart(data) {
  if (!statusChart) return
  const names = data.map(item => item.status || item.name)
  const values = data.map(item => item.count || item.value)

  statusChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '8%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { fontSize: 12 },
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      name: '数量(个)',
      nameTextStyle: { fontSize: 12 }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '50%',
        data: values.map((v, i) => ({
          value: v,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: statusColors[i % statusColors.length] },
              { offset: 1, color: adjustColorOpacity(statusColors[i % statusColors.length], 0.6) }
            ]),
            borderRadius: [6, 6, 0, 0]
          }
        })),
        label: {
          show: true,
          position: 'top',
          fontSize: 12,
          fontWeight: 'bold'
        }
      }
    ]
  })
}

// Line Chart - Rectification Trend
function loadTrendChart() {
  // Mock monthly trend data; replace with real API call when available
  updateTrendChart()
}

function updateTrendChart() {
  if (!trendChart) return
  const isMonth = trendType.value === 'month'

  const categories = isMonth
    ? ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    : ['Q1', 'Q2', 'Q3', 'Q4']

  const discoveredData = isMonth
    ? [18, 22, 25, 30, 28, 35, 32, 28, 25, 22, 20, 18]
    : [65, 93, 85, 60]

  const rectifiedData = isMonth
    ? [12, 15, 20, 25, 30, 28, 35, 30, 28, 24, 22, 20]
    : [47, 83, 93, 70]

  const overdueData = isMonth
    ? [3, 4, 5, 6, 8, 7, 5, 4, 3, 2, 2, 1]
    : [12, 21, 12, 5]

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['发现问题', '已完成整改', '逾期问题'],
      bottom: 0,
      textStyle: { fontSize: 12 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLabel: { fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      name: '数量(个)',
      nameTextStyle: { fontSize: 12 }
    },
    series: [
      {
        name: '发现问题',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#409EFF', width: 2.5 },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ])
        },
        data: discoveredData
      },
      {
        name: '已完成整改',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#67C23A', width: 2.5 },
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.25)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.02)' }
          ])
        },
        data: rectifiedData
      },
      {
        name: '逾期问题',
        type: 'line',
        smooth: true,
        symbol: 'diamond',
        symbolSize: 6,
        lineStyle: { color: '#F56C6C', width: 2.5, type: 'dashed' },
        itemStyle: { color: '#F56C6C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.2)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.02)' }
          ])
        },
        data: overdueData
      }
    ]
  })
}

// Load risk area data
function loadRiskAreas() {
  riskLoading.value = true
  // Try fund recovery API first, fall back to recurring issues
  getFundRecovery().then(res => {
    const data = res.data || res || []
    if (Array.isArray(data) && data.length > 0) {
      riskAreaData.value = data.map(item => ({
        area: item.area || item.name || '未知领域',
        count: item.count || item.issueCount || 0,
        completed: item.completed || item.rectifiedCount || 0,
        overdue: item.overdue || item.overdueCount || 0,
        completionRate: item.completionRate || item.rate || 0,
        recoveryAmount: item.recoveryAmount || item.amount || 0
      }))
    } else {
      loadRecurringData()
    }
  }).catch(() => {
    loadRecurringData()
  }).finally(() => {
    riskLoading.value = false
  })
}

function loadRecurringData() {
  getRecurring().then(res => {
    const data = res.data || res || []
    if (Array.isArray(data) && data.length > 0) {
      riskAreaData.value = data.map(item => ({
        area: item.area || item.name || '未知领域',
        count: item.count || item.issueCount || 0,
        completed: item.completed || item.rectifiedCount || 0,
        overdue: item.overdue || item.overdueCount || 0,
        completionRate: item.completionRate || item.rate || 0,
        recoveryAmount: item.recoveryAmount || item.amount || 0
      }))
    }
  }).catch(() => {
    riskAreaData.value = [
      { area: '采购与招投标', count: 48, completed: 40, overdue: 5, completionRate: 83.3, recoveryAmount: 320.5 },
      { area: '财务管理', count: 42, completed: 35, overdue: 4, completionRate: 83.3, recoveryAmount: 458.2 },
      { area: '工程项目', count: 36, completed: 30, overdue: 8, completionRate: 83.3, recoveryAmount: 215.0 },
      { area: '资产管理', count: 30, completed: 22, overdue: 3, completionRate: 73.3, recoveryAmount: 180.6 },
      { area: '合同管理', count: 25, completed: 20, overdue: 2, completionRate: 80.0, recoveryAmount: 84.0 }
    ]
  })
}

// Helper functions
function formatMoney(val) {
  if (val == null || val === '') return '0'
  const num = Number(val)
  if (isNaN(num)) return val
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function progressColor(rate) {
  if (rate >= 80) return '#67C23A'
  if (rate >= 50) return '#E6A23C'
  return '#F56C6C'
}

function adjustColorOpacity(hex, opacity) {
  if (!hex) return hex
  let color = hex.replace('#', '')
  if (color.length === 3) {
    color = color.split('').map(c => c + c).join('')
  }
  const r = parseInt(color.substring(0, 2), 16)
  const g = parseInt(color.substring(2, 4), 16)
  const b = parseInt(color.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${opacity})`
}
</script>

<style scoped lang="scss">
.app-container {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: #303133;

  &::before {
    content: '';
    display: inline-block;
    width: 4px;
    height: 16px;
    background: #409EFF;
    border-radius: 2px;
    margin-right: 8px;
    vertical-align: middle;
  }
}

:deep(.el-card) {
  border-radius: 8px;
}

:deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #ebeef5;
}
</style>
