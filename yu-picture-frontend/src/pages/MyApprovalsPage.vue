<template>
  <div id="myApprovalsPage" class="yx-page-shell">
    <h2 class="yx-page-title">我的审批申请</h2>
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
          <a-tag v-if="record.reviewStatus === 0" color="orange">待审核</a-tag>
          <a-tag v-else-if="record.reviewStatus === 1" color="green">已通过</a-tag>
          <a-tag v-else-if="record.reviewStatus === 2" color="red">已拒绝</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'reviewMessage'">
          <span>{{ record.reviewMessage || '-' }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ?? '-' }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

const onPageChange = (page: any) => {
  pagination.current = page.current
  pagination.pageSize = page.pageSize
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>
