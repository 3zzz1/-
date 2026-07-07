<template>
  <el-row :gutter="16">
    <el-col :span="6" v-for="(item, index) in items" :key="index">
      <div class="overview-card" :style="{ background: gradientColors[index % gradientColors.length] }">
        <div class="card-content">
          <div class="card-left">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-value">
              <span class="value-number">{{ item.value }}</span>
              <span class="value-unit" v-if="item.unit">{{ item.unit }}</span>
            </div>
          </div>
          <div class="card-right">
            <el-icon :size="48" :color="iconColors[index % iconColors.length]">
              <component :is="item.icon || 'DataAnalysis'" />
            </el-icon>
          </div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script setup name="OverviewCards">
defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const gradientColors = [
  'linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)',
  'linear-gradient(135deg, #67C23A 0%, #85ce61 100%)',
  'linear-gradient(135deg, #E6A23C 0%, #ebb563 100%)',
  'linear-gradient(135deg, #F56C6C 0%, #f78989 100%)'
]

const iconColors = [
  'rgba(255, 255, 255, 0.7)',
  'rgba(255, 255, 255, 0.7)',
  'rgba(255, 255, 255, 0.7)',
  'rgba(255, 255, 255, 0.7)'
]
</script>

<style scoped lang="scss">
.overview-card {
  border-radius: 8px;
  padding: 20px 24px;
  color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  }

  .card-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-left {
    flex: 1;
  }

  .card-title {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .card-value {
    display: flex;
    align-items: baseline;
    gap: 4px;

    .value-number {
      font-size: 28px;
      font-weight: 700;
      line-height: 1;
    }

    .value-unit {
      font-size: 14px;
      opacity: 0.85;
    }
  }

  .card-right {
    flex-shrink: 0;
    opacity: 0.6;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
