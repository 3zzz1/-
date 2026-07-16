<template>
  <div :class="{ 'hidden': hidden }" class="pagination-container">
    <el-pagination
      :background="background"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :layout="resolvedLayout"
      :page-sizes="pageSizes"
      :pager-count="resolvedPagerCount"
      :small="isMobile"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { scrollTo } from '@/utils/scroll-to'

const props = defineProps({
  total: {
    required: true,
    type: Number
  },
  page: {
    type: Number,
    default: 1
  },
  limit: {
    type: Number,
    default: 20
  },
  pageSizes: {
    type: Array,
    default() {
      return [10, 20, 30, 50]
    }
  },
  // 移动端页码按钮的数量端默认值5
  pagerCount: {
    type: Number,
    default: document.body.clientWidth < 992 ? 5 : 7
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  background: {
    type: Boolean,
    default: true
  },
  autoScroll: {
    type: Boolean,
    default: true
  },
  hidden: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits();
const isMobile = ref(window.innerWidth <= 768)
const desktopPageSize = ref(props.limit)
const resolvedLayout = computed(() => isMobile.value ? 'prev, pager, next' : props.layout)
const resolvedPagerCount = computed(() => isMobile.value ? 5 : props.pagerCount)

function handleViewportChange() {
  const nextMobile = window.innerWidth <= 768
  if (nextMobile === isMobile.value) return
  isMobile.value = nextMobile
  syncResponsivePageSize()
}

function syncResponsivePageSize() {
  const targetSize = isMobile.value ? 2 : desktopPageSize.value
  if (props.limit === targetSize) return
  emit('update:page', 1)
  emit('update:limit', targetSize)
  emit('pagination', { page: 1, limit: targetSize })
}

watch(() => props.limit, val => {
  if (!isMobile.value && val) desktopPageSize.value = val
})

onMounted(() => {
  window.addEventListener('resize', handleViewportChange)
  syncResponsivePageSize()
})
onBeforeUnmount(() => window.removeEventListener('resize', handleViewportChange))

const currentPage = computed({
  get() {
    return props.page
  },
  set(val) {
    emit('update:page', val)
  }
})
const pageSize = computed({
  get() {
    return props.limit
  },
  set(val){
    emit('update:limit', val)
  }
})
function handleSizeChange(val) {
  if (currentPage.value * val > props.total) {
    currentPage.value = 1
  }
  emit('pagination', { page: currentPage.value, limit: val })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}
function handleCurrentChange(val) {
  emit('pagination', { page: val, limit: pageSize.value })
  if (props.autoScroll) {
    scrollTo(0, 800)
  }
}

</script>

<style scoped>
.pagination-container {
  background: #fff;
  padding: 32px 16px;
}
.pagination-container.hidden {
  display: none;
}

@media (max-width: 768px) {
  .pagination-container {
    box-sizing: border-box;
    width: 100%;
    height: auto;
    min-height: 48px;
    margin: 12px 0 0;
    padding: 10px 0 !important;
    overflow: visible;
    background: transparent;
  }

  .pagination-container :deep(.el-pagination) {
    position: static !important;
    right: auto !important;
    display: flex;
    justify-content: center;
    width: 100%;
    padding: 0;
    white-space: nowrap;
  }

  .pagination-container :deep(.btn-prev),
  .pagination-container :deep(.btn-next),
  .pagination-container :deep(.el-pager li) {
    flex: 0 0 auto;
    min-width: 28px;
    margin: 0 2px;
  }
}
</style>
