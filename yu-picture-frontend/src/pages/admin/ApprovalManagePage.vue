<template>
  <div id="approvalManagePage" class="yx-page-shell">
    <h2 class="yx-page-title">审批管理</h2>
    <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
      <a-tab-pane key="0" tab="待审核" />
      <a-tab-pane key="1" tab="已通过" />
      <a-tab-pane key="2" tab="已拒绝" />
    </a-tabs>
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
        <template v-else-if="column.dataIndex === 'user'">
          <a-space>
            <a-avatar :size="24" :src="record.user?.userAvatar" />
            <span>{{ record.user?.userName ?? '-' }}</span>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'reviewStatus'">
          <a-tag v-if="record.reviewStatus === 0" color="orange">待审核</a-tag>
          <a-tag v-else-if="record.reviewStatus === 1" color="green">已通过</a-tag>
          <a-tag v-else-if="record.reviewStatus === 2" color="red">已拒绝</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ?? '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space v-if="record.reviewStatus === 0">
            <a-button type="primary" size="small" @click="doApprove(record)">通过</a-button>
            <a-button danger size="small" @click="doReject(record)">拒绝</a-button>
          </a-space>
          <span v-else>-</span>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="rejectModalVisible"
      title="拒绝理由"
      @ok="handleRejectSubmit"
    >
      <a-textarea v-model:value="rejectMessage" placeholder="请输入拒绝理由" :rows="3" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { doApprovalReviewUsingPost, listApprovalsUsingPost } from '@/api/pictureApprovalController'
import { message } from 'ant-design-vue'

const columns = [
  { title: '图片', dataIndex: 'pictureUrl', width: 80 },
  { title: '图片名称', dataIndex: 'pictureName' },
  { title: '目标空间', dataIndex: 'spaceName' },
  { title: '申请人', dataIndex: 'user' },
  { title: '状态', dataIndex: 'reviewStatus' },
  { title: '申请时间', dataIndex: 'createTime' },
  { title: '操作', dataIndex: 'action', width: 160 },
]

const dataList = ref<API.PictureApprovalVO[]>([])
const loading = ref(false)
const activeTab = ref('0')

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listApprovalsUsingPost({
      current: pagination.current,
      pageSize: pagination.pageSize,
      reviewStatus: Number(activeTab.value),
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

const onPageChange = (page: any) => {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchData()
}

const onTabChange = () => {
  pagination.current = 1
  fetchData()
}

const doApprove = async (record: API.PictureApprovalVO) => {
  try {
    const res = await doApprovalReviewUsingPost({
      id: record.id,
      reviewStatus: 1,
      reviewMessage: '审核通过',
    })
    if (res.data.code === 0) {
      message.success('已通过')
      fetchData()
    } else {
      message.error(res.data.message || '操作失败')
    }
  } catch (e: any) {
    message.error('操作失败：' + e.message)
  }
}

const rejectModalVisible = ref(false)
const rejectMessage = ref('')
const currentRejectId = ref<number>()

const doReject = (record: API.PictureApprovalVO) => {
  currentRejectId.value = record.id
  rejectMessage.value = ''
  rejectModalVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!currentRejectId.value) return
  try {
    const res = await doApprovalReviewUsingPost({
      id: currentRejectId.value,
      reviewStatus: 2,
      reviewMessage: rejectMessage.value || '审核拒绝',
    })
    if (res.data.code === 0) {
      message.success('已拒绝')
      rejectModalVisible.value = false
      fetchData()
    } else {
      message.error(res.data.message || '操作失败')
    }
  } catch (e: any) {
    message.error('操作失败：' + e.message)
  }
}

onMounted(() => {
  fetchData()
})
</script>
