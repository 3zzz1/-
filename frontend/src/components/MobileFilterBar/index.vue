<template>
  <div class="mobile-filter-bar">
    <el-input
      :model-value="modelValue"
      :placeholder="placeholder"
      prefix-icon="Search"
      clearable
      @update:model-value="$emit('update:modelValue', $event)"
      @keyup.enter="$emit('search')"
      @clear="$emit('search')"
    />
    <el-tooltip content="搜索" placement="bottom">
      <el-button type="primary" icon="Search" circle @click="$emit('search')" />
    </el-tooltip>
    <el-badge :value="activeCount" :hidden="!activeCount" class="mobile-filter-badge">
      <el-tooltip content="筛选" placement="bottom">
        <el-button icon="Filter" circle @click="drawerOpen = true" />
      </el-tooltip>
    </el-badge>
  </div>

  <el-drawer
    v-model="drawerOpen"
    class="mobile-filter-drawer"
    title="筛选条件"
    direction="rtl"
    size="86%"
    append-to-body
  >
    <div class="mobile-filter-drawer-body">
      <slot />
    </div>
    <template #footer>
      <div class="mobile-filter-drawer-footer">
        <el-button icon="Refresh" @click="resetFilters">重置</el-button>
        <el-button type="primary" icon="Search" @click="applyFilters">查看结果</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '搜索' },
  activeCount: { type: Number, default: 0 }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])
const drawerOpen = ref(false)

function applyFilters() {
  drawerOpen.value = false
  emit('search')
}

function resetFilters() {
  drawerOpen.value = false
  emit('reset')
}
</script>

<style>
.mobile-filter-bar {
  display: none;
}

@media (max-width: 768px) {
  .mobile-filter-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 36px 36px;
    align-items: center;
    gap: 8px;
    padding: 8px;
    margin-bottom: 10px;
    border: 1px solid #e4eaf2;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 5px 16px rgba(42, 60, 84, 0.05);
  }

  .mobile-filter-bar .el-input__wrapper {
    padding-left: 10px;
    border-radius: 6px;
    background: #f5f7fa;
    box-shadow: none;
  }

  .mobile-filter-bar .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  .mobile-filter-bar > .el-button,
  .mobile-filter-bar .mobile-filter-badge,
  .mobile-filter-bar .mobile-filter-badge .el-button {
    width: 36px;
    height: 36px;
    margin: 0;
  }

  .mobile-filter-drawer .el-drawer__header {
    padding: 16px 18px;
    margin-bottom: 0;
    border-bottom: 1px solid #edf1f6;
  }

  .mobile-filter-drawer .el-drawer__body {
    padding: 16px 18px;
    overflow-y: auto;
  }

  .mobile-filter-drawer .el-form-item {
    display: block;
    margin: 0 0 18px;
  }

  .mobile-filter-drawer .el-form-item__label {
    display: block;
    height: auto;
    margin-bottom: 7px;
    color: #52657d;
    line-height: 1.4;
    text-align: left;
  }

  .mobile-filter-drawer .el-input,
  .mobile-filter-drawer .el-select,
  .mobile-filter-drawer .el-date-editor {
    width: 100% !important;
  }

  .mobile-filter-drawer .el-drawer__footer {
    padding: 12px 18px;
    border-top: 1px solid #e8edf4;
  }

  .mobile-filter-drawer-footer {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .mobile-filter-drawer-footer .el-button {
    width: 100%;
    margin: 0;
  }
}
</style>
