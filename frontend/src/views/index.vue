<template>
  <div class="app-container home">
    <!-- 系统标题 -->
    <div class="hero-section">
      <h1 class="system-title">高校一体化智慧审计平台系统</h1>
      <p class="system-subtitle">统一管控校内审计、外部巡视、上级审计机关发现的全部问题，实现问题整改全生命周期线上闭环跟踪</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/rectification/issue')">
          <div class="stat-icon" style="background: #ecf5ff; color: #409EFF">
            <el-icon :size="32"><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalIssues }}</div>
            <div class="stat-label">问题台账总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/rectification/task')">
          <div class="stat-icon" style="background: #fdf6ec; color: #E6A23C">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inProgress }}</div>
            <div class="stat-label">整改进行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/rectification/closure')">
          <div class="stat-icon" style="background: #f0f9eb; color: #67C23A">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成销号</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/rectification/statistics')">
          <div class="stat-icon" style="background: #fef0f0; color: #F56C6C">
            <el-icon :size="32"><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdue }}</div>
            <div class="stat-label">逾期未整改</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能入口 -->
    <el-row :gutter="16" class="section">
      <el-col :span="24">
        <el-card shadow="never" class="feature-card">
          <template #header><h3>审计整改管理</h3></template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="8" :lg="4" v-for="item in features" :key="item.title">
              <div class="feature-item" @click="$router.push(item.path)">
                <div class="feature-icon" :style="{ background: item.bg, color: item.color }">
                  <el-icon :size="28"><component :is="item.icon" /></el-icon>
                </div>
                <div class="feature-title">{{ item.title }}</div>
                <div class="feature-desc">{{ item.desc }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 全流程说明 -->
    <el-row :gutter="16" class="section">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header><h3>整改业务流程</h3></template>
          <el-steps direction="vertical" :space="30" finish-status="success">
            <el-step title="问题台账生成" description="内源审计自动同步 + 外源手动录入，统一纳入整改跟踪体系" />
            <el-step title="整改任务下发" description="审计处批量下发，指定整改单位/联络人/截止时限，多端消息触达" />
            <el-step title="整改单位执行" description="确认分办→方案编制→材料填报→报告生成→销号申请" />
            <el-step title="审计处审核销号" description="区分完成/不到位，通过则正式销号归档，不通过则驳回二次整改" />
            <el-step title="多维数据分析" description="完成率、逾期率、问题分布、资金挽回规模，自动识别高频风险" />
          </el-steps>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header><h3>权限管理体系</h3></template>
          <div class="perm-list">
            <el-tag type="" size="large" class="perm-tag">校领导：全校汇总数据，屏蔽底层明细</el-tag>
            <el-tag type="success" size="large" class="perm-tag">审计处长：全量数据，全系统功能</el-tag>
            <el-tag type="warning" size="large" class="perm-tag">项目组长：负责项目，复核审核权限</el-tag>
            <el-tag type="" size="large" class="perm-tag">审计人员：分配项目，作业录入权限</el-tag>
            <el-tag type="info" size="large" class="perm-tag">被审单位：仅本单位数据，联络员可操作</el-tag>
            <el-tag type="danger" size="large" class="perm-tag">中介机构：临时授权，到期自动回收</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '@/api/rectification/statistics'

const stats = ref({ totalIssues: 0, inProgress: 0, completed: 0, overdue: 0 })

const features = [
  { title: '问题台账', desc: '问题录入与管理', icon: 'List', path: '/rectification/issue', bg: '#ecf5ff', color: '#409EFF' },
  { title: '整改任务', desc: '任务下发与跟踪', icon: 'EditPen', path: '/rectification/task', bg: '#fdf6ec', color: '#E6A23C' },
  { title: '销号管理', desc: '审核与销号', icon: 'CircleCheck', path: '/rectification/closure', bg: '#f0f9eb', color: '#67C23A' },
  { title: '整改分析', desc: '多维统计分析', icon: 'DataAnalysis', path: '/rectification/statistics', bg: '#eff2f7', color: '#909399' },
  { title: '方案管理', desc: '方案编制与延期', icon: 'Document', path: '/rectification/task', bg: '#f4f4fe', color: '#B37FEB' },
  { title: '我的任务', desc: '整改单位入口', icon: 'User', path: '/rectification/my-tasks', bg: '#e8f4fd', color: '#36CFC9' },
]

onMounted(() => {
  getOverview().then(res => {
    const data = res.data || {}
    stats.value = {
      totalIssues: data.totalIssues || 0,
      inProgress: data.inProgressCount || 0,
      completed: data.completedCount || 0,
      overdue: data.overdueCount || 0,
    }
  }).catch(() => {})
})
</script>

<style scoped lang="scss">
.home {
  padding: 10px;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
}
.hero-section {
  text-align: center;
  padding: 40px 20px 30px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  color: #fff;
}
.system-title { font-size: 28px; margin: 0 0 12px; font-weight: 600; }
.system-subtitle { font-size: 14px; opacity: 0.85; margin: 0; }

.stats-row { margin-bottom: 16px; }
.stat-card {
  cursor: pointer; transition: transform 0.2s;
  &:hover { transform: translateY(-3px); }
}
.stat-card :deep(.el-card__body) { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-right: 16px; flex-shrink: 0; }
.stat-info .stat-value { font-size: 28px; font-weight: 700; color: #303133; }
.stat-info .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.section { margin-bottom: 16px; }
.feature-item {
  text-align: center; padding: 20px 10px; cursor: pointer; border-radius: 8px; transition: all 0.2s;
  &:hover { background: #f5f7fa; transform: translateY(-2px); }
}
.feature-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; }
.feature-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.feature-desc { font-size: 12px; color: #909399; }

.perm-list { display: flex; flex-wrap: wrap; gap: 10px; }
.perm-tag { padding: 10px 14px; font-size: 13px; border-radius: 6px; }
</style>
