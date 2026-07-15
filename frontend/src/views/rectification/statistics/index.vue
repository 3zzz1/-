<template>
  <div class="app-container rect-stat-page">
    <overview-cards :items="overviewItems" />

    <el-row :gutter="16" class="section-row chart-grid">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>问题类型分布</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="hover" class="stat-card">
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
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header trend-header">
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
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>高频风险领域与资金挽回</span>
            </div>
          </template>

          <el-table v-if="!isMobile" :data="riskAreaData" v-loading="riskLoading" stripe>
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

          <div v-else v-loading="riskLoading" class="mobile-list">
            <div v-for="item in riskAreaData" :key="item.area" class="mobile-data-card">
              <div class="mobile-card-title">
                <span>{{ item.area }}</span>
                <el-tag :type="item.overdue > 0 ? 'danger' : 'success'" effect="plain">
                  逾期 {{ item.overdue || 0 }}
                </el-tag>
              </div>
              <div class="mobile-metrics">
                <div>
                  <strong>{{ item.count || 0 }}</strong>
                  <span>问题数量</span>
                </div>
                <div>
                  <strong>{{ item.completed || 0 }}</strong>
                  <span>已整改</span>
                </div>
                <div>
                  <strong>{{ formatMoney(item.recoveryAmount) }}</strong>
                  <span>资金挽回</span>
                </div>
              </div>
              <el-progress
                :percentage="Number(item.completionRate || 0)"
                :color="progressColor(item.completionRate)"
                :stroke-width="8"
              />
              <div class="mobile-suggestion">{{ riskSuggestion(item) }}</div>
            </div>
            <el-empty v-if="!riskLoading && riskAreaData.length === 0" description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="24">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>重复发生问题统计</span>
            </div>
          </template>

          <el-table v-if="!isMobile" :data="recurringData" v-loading="recurringLoading" stripe>
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

          <div v-else v-loading="recurringLoading" class="mobile-list">
            <div v-for="item in recurringData" :key="`${item.issueType}-${item.area}`" class="mobile-data-card">
              <div class="mobile-card-title">
                <span>{{ item.issueType || '未命名问题' }}</span>
                <el-tag :type="riskTag(item.riskLevel)" effect="plain">{{ item.riskLevel || '-' }}</el-tag>
              </div>
              <div class="mobile-subtitle">{{ item.area || '未分类领域' }}</div>
              <div class="mobile-metrics">
                <div>
                  <strong>{{ item.occurrences || 0 }}</strong>
                  <span>发生次数</span>
                </div>
                <div>
                  <strong>{{ item.completed || 0 }}</strong>
                  <span>已整改</span>
                </div>
                <div>
                  <strong :class="{ danger: item.overdue > 0 }">{{ item.overdue || 0 }}</strong>
                  <span>逾期数</span>
                </div>
              </div>
              <div class="mobile-suggestion">{{ item.suggestion || '暂无建议' }}</div>
            </div>
            <el-empty v-if="!recurringLoading && recurringData.length === 0" description="暂无数据" :image-size="80" />
          </div>
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
const categoryData = ref([])
const statusData = ref([])
const trendData = ref([])
const isMobile = ref(window.innerWidth <= 768)

const overviewItems = ref([
  { title: '问题总数', value: 0, unit: '个', icon: 'DataAnalysis' },
  { title: '整改完成率', value: '0%', unit: '', icon: 'CircleCheck' },
  { title: '逾期率', value: '0%', unit: '', icon: 'Warning' },
  { title: '资金挽回规模', value: '￥0.00', unit: '', icon: 'Money' }
])

const categoryColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#64748b', '#14b8a6']
const statusColors = ['#64748b', '#3b82f6', '#f59e0b', '#10b981', '#8b5cf6']

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
  const nextMobile = window.innerWidth <= 768
  if (nextMobile !== isMobile.value) {
    isMobile.value = nextMobile
    updatePieChart(categoryData.value)
    updateBarChart(statusData.value)
    updateTrendChart(trendData.value)
  }
  setTimeout(() => {
    categoryChart?.resize()
    statusChart?.resize()
    trendChart?.resize()
  }, 80)
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
      { title: '资金挽回规模', value: `￥${formatMoney(data.totalRecoveryAmount || 0)}`, unit: '', icon: 'Money' }
    ]
  })
}

function loadCategoryChart() {
  getByCategory().then(res => {
    categoryData.value = res.data || []
    updatePieChart(categoryData.value)
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
    legend: {
      type: 'scroll',
      orient: isMobile.value ? 'horizontal' : 'vertical',
      left: isMobile.value ? 'center' : undefined,
      right: isMobile.value ? undefined : 8,
      bottom: isMobile.value ? 0 : undefined,
      top: isMobile.value ? undefined : 'center'
    },
    series: [{
      name: '问题类型',
      type: 'pie',
      radius: isMobile.value ? ['38%', '62%'] : ['45%', '70%'],
      center: isMobile.value ? ['50%', '44%'] : ['38%', '50%'],
      label: { show: !isMobile.value, formatter: '{b}\n{d}%' },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      data: chartData
    }]
  }, true)
}

function loadStatusChart() {
  getByStatus().then(res => {
    statusData.value = res.data || []
    updateBarChart(statusData.value)
  })
}

function updateBarChart(data) {
  if (!statusChart) return
  const names = data.map(item => item.status || item.name)
  const values = data.map(item => item.count || item.value || 0)
  statusChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: {
      left: isMobile.value ? 8 : '3%',
      right: isMobile.value ? 8 : '8%',
      bottom: isMobile.value ? 36 : '3%',
      top: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { interval: 0, rotate: isMobile.value ? 28 : 0 }
    },
    yAxis: { type: 'value', name: isMobile.value ? '' : '数量(个)' },
    series: [{
      name: '数量',
      type: 'bar',
      barWidth: isMobile.value ? '42%' : '50%',
      label: { show: true, position: 'top' },
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: statusColors[index % statusColors.length],
          borderRadius: [6, 6, 0, 0]
        }
      }))
    }]
  }, true)
}

function loadTrendChart() {
  getTrend(trendType.value).then(res => {
    trendData.value = res.data || []
    updateTrendChart(trendData.value)
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
    legend: {
      data: ['发现问题', '已完成整改', '逾期问题'],
      bottom: 0,
      itemWidth: isMobile.value ? 10 : 18,
      textStyle: { fontSize: isMobile.value ? 11 : 12 }
    },
    grid: {
      left: isMobile.value ? 8 : '3%',
      right: isMobile.value ? 10 : '4%',
      bottom: isMobile.value ? 52 : '12%',
      top: isMobile.value ? 20 : '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: periods,
      axisLabel: { rotate: isMobile.value ? 28 : 0 }
    },
    yAxis: { type: 'value', name: isMobile.value ? '' : '数量(个)' },
    series: [
      { name: '发现问题', type: 'line', smooth: true, data: discovered, itemStyle: { color: '#3b82f6' } },
      { name: '已完成整改', type: 'line', smooth: true, data: completed, itemStyle: { color: '#10b981' } },
      { name: '逾期问题', type: 'line', smooth: true, data: overdue, itemStyle: { color: '#ef4444' }, lineStyle: { type: 'dashed' } }
    ]
  }, true)
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
  if (value >= 80) return '#10b981'
  if (value >= 50) return '#f59e0b'
  return '#ef4444'
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
.rect-stat-page {
  padding: 16px;
  background: #f5f7fb;
}

.section-row {
  margin-top: 16px;
}

.chart-grid {
  row-gap: 16px;
}

.stat-card {
  border-radius: 8px;
  border: 1px solid #e8edf5;
  overflow: hidden;
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
  gap: 12px;
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;

  &::before {
    content: '';
    flex: 0 0 auto;
    width: 4px;
    height: 16px;
    background: #3b82f6;
    border-radius: 2px;
  }

  > span {
    flex: 1;
    min-width: 0;
  }
}

.danger {
  color: #ef4444;
}

.mobile-list {
  display: grid;
  gap: 12px;
  min-height: 120px;
}

.mobile-data-card {
  padding: 14px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: #fff;
}

.mobile-card-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  color: #1f2937;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;

  span:first-child {
    min-width: 0;
    word-break: break-word;
  }
}

.mobile-subtitle {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

.mobile-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin: 12px 0;

  div {
    min-width: 0;
    padding: 10px 8px;
    border-radius: 8px;
    background: #f8fafc;
  }

  strong,
  span {
    display: block;
    min-width: 0;
  }

  strong {
    color: #0f172a;
    font-size: 16px;
    line-height: 1.2;
    word-break: break-all;
  }

  span {
    margin-top: 4px;
    color: #64748b;
    font-size: 12px;
  }
}

.mobile-suggestion {
  margin-top: 10px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

:deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom-color: #edf2f7;
}

:deep(.el-card__body) {
  padding: 16px;
}

@media (max-width: 768px) {
  .rect-stat-page {
    padding: 10px;
  }

  .section-row {
    margin-top: 12px;
  }

  .chart-grid :deep(.el-col + .el-col) {
    margin-top: 12px;
  }

  .chart {
    height: 280px;
  }

  .trend-chart {
    height: 300px;
  }

  .card-header {
    align-items: flex-start;
    font-size: 14px;
  }

  .trend-header {
    display: grid;
    grid-template-columns: 1fr;

    :deep(.el-radio-group) {
      display: grid;
      grid-template-columns: 1fr 1fr;
      width: 100%;
    }

    :deep(.el-radio-button__inner) {
      width: 100%;
      padding: 8px 10px;
    }
  }

  :deep(.el-card__header) {
    padding: 12px 14px;
  }

  :deep(.el-card__body) {
    padding: 12px;
  }

  .mobile-metrics {
    grid-template-columns: 1fr 1fr;

    div:last-child {
      grid-column: 1 / -1;
    }
  }
}
</style>
