<template>
  <div id="myApprovalsPage" class="yx-page-shell">
    <div class="page-header">
      <div>
        <h2 class="yx-page-title">我的审批申请</h2>
        <p class="yx-page-sub">查看你提交的图片添加申请的审核进度</p>
      </div>
      <div class="header-decoration">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="38" stroke="url(#g1)" stroke-width="1.5" opacity="0.5" />
          <circle cx="40" cy="40" r="28" stroke="url(#g2)" stroke-width="1" opacity="0.35" />
          <path d="M28 42L36 50L52 34" stroke="#6366f1" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
          <defs>
            <linearGradient id="g1" x1="0" y1="0" x2="80" y2="80"><stop stop-color="#6366f1" /><stop offset="1" stop-color="#a78bfa" /></linearGradient>
            <linearGradient id="g2" x1="80" y1="0" x2="0" y2="80"><stop stop-color="#0ea5e9" /><stop offset="1" stop-color="#6366f1" /></linearGradient>
          </defs>
        </svg>
      </div>
    </div>
    <a-row :gutter="[16, 16]" class="stats-row">
      <a-col :span="8">
        <div class="yx-stat-card">
          <div class="yx-stat-icon amber"><ClockCircleOutlined /></div>
          <div>
            <div class="yx-stat-value">{{ stats.pending }}</div>
            <div class="yx-stat-label">待审核</div>
          </div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="yx-stat-card">
          <div class="yx-stat-icon green"><CheckCircleOutlined /></div>
          <div>
            <div class="yx-stat-value">{{ stats.approved }}</div>
            <div class="yx-stat-label">已通过</div>
          </div>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="yx-stat-card">
          <div class="yx-stat-icon red"><CloseCircleOutlined /></div>
          <div>
            <div class="yx-stat-value">{{ stats.rejected }}</div>
            <div class="yx-stat-label">已拒绝</div>
          </div>
        </div>
      </a-col>
    </a-row>
    <div class="yx-divider-dot"><span /><span /></div>
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="pagination"
      @change="onPageChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'pictureUrl'">
          <a-image
            :src="record.pictureUrl"
            :width="60"
            :height="60"
            style="object-fit: cover; border-radius: 8px"
          />
        </template>
        <template v-else-if="column.dataIndex === 'pictureName'">
          <a-typography-text strong>{{ record.pictureName ?? '-' }}</a-typography-text>
        </template>
        <template v-else-if="column.dataIndex === 'spaceName'">
          <a-tag>{{ record.spaceName ?? '-' }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'reviewStatus'">
          <a-tag v-if="record.reviewStatus === 0" color="orange"><span class="yx-dot-pending" />待审核</a-tag>
          <a-tag v-else-if="record.reviewStatus === 1" color="green"><span class="yx-dot-approved" />已通过</a-tag>
          <a-tag v-else-if="record.reviewStatus === 2" color="red"><span class="yx-dot-rejected" />已拒绝</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'reviewMessage'">
          <a-typography-text v-if="record.reviewMessage" type="secondary" :ellipsis="{ tooltip: true }">{{ record.reviewMessage }}</a-typography-text>
          <span v-else style="color: #cbd5e1">-</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          <span style="color: #94a3b8; font-size: 0.85rem">{{ record.createTime ?? '-' }}</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'
import { listMyApprovalsUsingPost } from '@/api/pictureApprovalController'
import { message } from 'ant-design-vue'

const columns = [
  { title: '图片', dataIndex: 'pictureUrl', width: 80 },
  { title: '图片名称', dataIndex: 'pictureName' },
  { title: '目标空间', dataIndex: 'spaceName' },
  { title: '状态', dataIndex: 'reviewStatus' },
  { title: '审核信息', dataIndex: 'reviewMessage' },
  { title: '申请时间', dataIndex: 'createTime' },
]

const dataList = ref<API.PictureApprovalVO[]>([])
const loading = ref(false)

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const stats = reactive({ pending: 0, approved: 0, rejected: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMyApprovalsUsingPost({
      current: pagination.current,
      pageSize: pagination.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      pagination.total = res.data.data.total ?? 0
    } else {
      message.error('获取审批列表失败：' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取审批列表失败：' + e.message)
  }
  loading.value = false
}

const fetchStats = async () => {
  try {
    const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
      listMyApprovalsUsingPost({ current: 1, pageSize: 1, reviewStatus: 0 }),
      listMyApprovalsUsingPost({ current: 1, pageSize: 1, reviewStatus: 1 }),
      listMyApprovalsUsingPost({ current: 1, pageSize: 1, reviewStatus: 2 }),
    ])
    stats.pending = pendingRes.data.data?.total ?? 0
    stats.approved = approvedRes.data.data?.total ?? 0
    stats.rejected = rejectedRes.data.data?.total ?? 0
  } catch (_) { /* ignore */ }
}

const onPageChange = (page: any) => {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
#myApprovalsPage .page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

#myApprovalsPage .header-decoration {
  flex-shrink: 0;
  opacity: 0.7;
}

#myApprovalsPage .stats-row {
  margin-bottom: 0.25rem;
}
</style>
