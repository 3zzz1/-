<template>
  <div class="app-container">
    <overview-cards :items="overviewItems" />

    <el-row :gutter="16" class="section-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>问题类型分布</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>整改状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>整改趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendChart">
                <el-radio-button label="month">按月</el-radio-button>
                <el-radio-button label="quarter">按季度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="trend-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>高频风险领域与资金挽回</span>
            </div>
          </template>
          <el-table :data="riskAreaData" v-loading="riskLoading" stripe>
            <el-table-column prop="area" label="风险领域" min-width="160" show-overflow-tooltip />
            <el-table-column prop="count" label="问题数量" width="110" align="center" sortable />
            <el-table-column prop="completed" label="已整改" width="100" align="center" />
            <el-table-column prop="overdue" label="逾期数" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.overdue > 0 ? 'danger' : 'success'" effect="plain">
                  {{ scope.row.overdue }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="completionRate" label="整改率" width="150" align="center">
              <template #default="scope">
                <el-progress
                  :percentage="Number(scope.row.completionRate || 0)"
                  :color="progressColor(scope.row.completionRate)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column prop="recoveryAmount" label="资金挽回规模" width="150" align="right">
              <template #default="scope">
                {{ formatMoney(scope.row.recoveryAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="分析提示" min-width="240" show-overflow-tooltip>
              <template #default="scope">
                {{ riskSuggestion(scope.row) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>重复发生问题统计</span>
            </div>
          </template>
          <el-table :data="recurringData" v-loading="recurringLoading" stripe>
            <el-table-column prop="issueType" label="重复问题/风险点" min-width="220" show-overflow-tooltip />
            <el-table-column prop="area" label="领域" width="120" align="center" />
            <el-table-column prop="occurrences" label="发生次数" width="110" align="center" sortable />
            <el-table-column prop="completed" label="已整改" width="100" align="center" />
            <el-table-column prop="overdue" label="逾期数" width="100" align="center">
              <template #default="scope">
                <span :class="{ danger: scope.row.overdue > 0 }">{{ scope.row.overdue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级" width="110" align="center">
              <template #default="scope">
                <el-tag :type="riskTag(scope.row.riskLevel)" effect="plain">{{ scope.row.riskLevel || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="suggestion" label="审计计划建议" min-width="260" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="RectificationStatistics">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import {
  getOverview,
  getByCategory,
  getByStatus,
  getFundRecovery,
  getRecurring,
  getTrend
} from '@/api/rectification/statistics'
import OverviewCards from './components/OverviewCards.vue'

const categoryChartRef = ref(null)
const statusChartRef = ref(null)
const trendChartRef = ref(null)
let categoryChart = null
let statusChart = null
let trendChart = null

const trendType = ref('month')
const riskLoading = ref(false)
const recurringLoading = ref(false)
const riskAreaData = ref([])
const recurringData = ref([])

const overviewItems = ref([
  { title: '问题总数', value: 0, unit: '个', icon: 'DataAnalysis' },
  { title: '整改完成率', value: '0%', unit: '', icon: 'CircleCheck' },
  { title: '逾期率', value: '0%', unit: '', icon: 'Warning' },
  { title: '资金挽回规模', value: '¥0.00', unit: '', icon: 'Money' }
])

const categoryColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#36CFC9']
const statusColors = ['#909399', '#409EFF', '#E6A23C', '#67C23A', '#B37FEB']

onMounted(() => {
  nextTick(() => {
    initCharts()
    loadAllData()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  statusChart?.dispose()
  trendChart?.dispose()
})

function initCharts() {
  categoryChart = categoryChartRef.value ? echarts.init(categoryChartRef.value) : null
  statusChart = statusChartRef.value ? echarts.init(statusChartRef.value) : null
  trendChart = trendChartRef.value ? echarts.init(trendChartRef.value) : null
}

function handleResize() {
  categoryChart?.resize()
  statusChart?.resize()
  trendChart?.resize()
}

function loadAllData() {
  loadOverview()
  loadCategoryChart()
  loadStatusChart()
  loadTrendChart()
  loadRiskAreas()
  loadRecurring()
}

function loadOverview() {
  getOverview().then(res => {
    const data = res.data || {}
    overviewItems.value = [
      { title: '问题总数', value: data.totalIssues || 0, unit: '个', icon: 'DataAnalysis' },
      { title: '整改完成率', value: `${data.completionRate || 0}%`, unit: '', icon: 'CircleCheck' },
      { title: '逾期率', value: `${data.overdueRate || 0}%`, unit: '', icon: 'Warning' },
      { title: '资金挽回规模', value: `¥${formatMoney(data.totalRecoveryAmount || 0)}`, unit: '', icon: 'Money' }
    ]
  })
}

function loadCategoryChart() {
  getByCategory().then(res => {
    const data = res.data || []
    updatePieChart(data)
  })
}

function updatePieChart(data) {
  if (!categoryChart) return
  const chartData = data.map((item, index) => ({
    name: item.category || item.name || '未分类',
    value: item.count || item.value || 0,
    itemStyle: { color: categoryColors[index % categoryColors.length] }
  }))
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { type: 'scroll', orient: 'vertical', right: 10, top: 'center' },
    series: [{
      name: '问题类型',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['38%', '50%'],
      label: { formatter: '{b}\n{d}%' },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      data: chartData
    }]
  })
}

function loadStatusChart() {
  getByStatus().then(res => {
    const data = res.data || []
    updateBarChart(data)
  })
}

function updateBarChart(data) {
  if (!statusChart) return
  const names = data.map(item => item.status || item.name)
  const values = data.map(item => item.count || item.value || 0)
  statusChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', name: '数量(个)' },
    series: [{
      name: '数量',
      type: 'bar',
      barWidth: '50%',
      label: { show: true, position: 'top' },
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: statusColors[index % statusColors.length],
          borderRadius: [6, 6, 0, 0]
        }
      }))
    }]
  })
}

function loadTrendChart() {
  getTrend(trendType.value).then(res => {
    updateTrendChart(res.data || [])
  })
}

function updateTrendChart(data) {
  if (!trendChart) return
  const periods = data.map(item => item.period)
  const discovered = data.map(item => item.discovered || 0)
  const completed = data.map(item => item.completed || 0)
  const overdue = data.map(item => item.overdue || 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['发现问题', '已完成整改', '逾期问题'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: periods },
    yAxis: { type: 'value', name: '数量(个)' },
    series: [
      { name: '发现问题', type: 'line', smooth: true, data: discovered, itemStyle: { color: '#409EFF' } },
      { name: '已完成整改', type: 'line', smooth: true, data: completed, itemStyle: { color: '#67C23A' } },
      { name: '逾期问题', type: 'line', smooth: true, data: overdue, itemStyle: { color: '#F56C6C' }, lineStyle: { type: 'dashed' } }
    ]
  })
}

function loadRiskAreas() {
  riskLoading.value = true
  getFundRecovery().then(res => {
    riskAreaData.value = (res.data || []).map(item => ({
      area: item.area || '未知领域',
      count: item.count || 0,
      completed: item.completed || 0,
      overdue: item.overdue || 0,
      completionRate: Number(item.completionRate || 0),
      recoveryAmount: item.recoveryAmount || 0
    }))
  }).finally(() => {
    riskLoading.value = false
  })
}

function loadRecurring() {
  recurringLoading.value = true
  getRecurring().then(res => {
    recurringData.value = res.data || []
  }).finally(() => {
    recurringLoading.value = false
  })
}

function formatMoney(val) {
  const num = Number(val || 0)
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function progressColor(rate) {
  const value = Number(rate || 0)
  if (value >= 80) return '#67C23A'
  if (value >= 50) return '#E6A23C'
  return '#F56C6C'
}

function riskTag(level) {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  if (level === '低') return 'success'
  return 'info'
}

function riskSuggestion(row) {
  if (row.overdue > 0) return '存在逾期整改，建议列入重点跟踪领域。'
  if (row.count >= 5) return '问题发生频次较高，建议纳入下一年度审计重点。'
  return '保持常规监督，关注整改长效机制。'
}
</script>

<style scoped lang="scss">
.app-container {
  padding: 16px;
}

.section-row {
  margin-top: 16px;
}

.chart {
  height: 350px;
}

.trend-chart {
  height: 380px;
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
  }
}

.danger {
  color: #f56c6c;
}

:deep(.el-card) {
  border-radius: 8px;
}

:deep(.el-card__header) {
  padding: 14px 20px;
}
</style>
