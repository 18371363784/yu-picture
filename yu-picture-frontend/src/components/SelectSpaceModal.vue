<template>
  <a-modal
    title="选择目标空间"
    :open="visible"
    :confirm-loading="submitLoading"
    @ok="handleSubmit"
    @cancel="handleClose"
    ok-text="提交申请"
    cancel-text="取消"
  >
    <a-form layout="vertical">
      <a-form-item label="图片">
        <a-card size="small">
          <a-image :src="pictureUrl" style="max-height: 120px; object-fit: cover" />
          <div style="margin-top: 8px; color: #666">{{ pictureName }}</div>
        </a-card>
      </a-form-item>
      <a-form-item label="目标空间" required>
        <a-select
          v-model:value="selectedSpaceId"
          placeholder="请选择空间"
          style="width: 100%"
          :loading="spaceLoading"
        >
          <a-select-opt-group v-if="privateSpaces.length" label="我的私有空间">
            <a-select-option
              v-for="space in privateSpaces"
              :key="space.id"
              :value="space.id"
            >
              {{ space.spaceName }}
            </a-select-option>
          </a-select-opt-group>
          <a-select-opt-group v-if="teamSpaces.length" label="我的团队空间">
            <a-select-option
              v-for="space in teamSpaces"
              :key="space.id"
              :value="space.id"
            >
              {{ space.spaceName }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-item>
      <a-alert
        type="info"
        message="提交后需等待管理员审核通过，图片才会添加到目标空间"
        show-icon
        style="margin-top: 12px"
      />
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { addApprovalUsingPost } from '@/api/pictureApprovalController'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { SPACE_TYPE_ENUM } from '@/constants/space'

interface Props {
  visible: boolean
  pictureId: number
  pictureUrl?: string
  pictureName?: string
  onSuccess?: () => void
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const loginUserStore = useLoginUserStore()

const selectedSpaceId = ref<number>()
const privateSpaces = ref<{ id: number; spaceName: string }[]>([])
const teamSpaces = ref<{ id: number; spaceName: string }[]>([])
const spaceLoading = ref(false)
const submitLoading = ref(false)

const fetchSpaces = async () => {
  spaceLoading.value = true
  privateSpaces.value = []
  teamSpaces.value = []
  const seenIds = new Set<number>()
  try {
    const userId = loginUserStore.loginUser.id
    if (userId) {
      // 查询用户创建的空间
      const res = await listSpaceVoByPageUsingPost({ userId, current: 1, pageSize: 20 })
      if (res.data.code === 0 && res.data.data?.records) {
        for (const space of res.data.data.records) {
          if (space.id && !seenIds.has(space.id)) {
            seenIds.add(space.id)
            if (space.spaceType === SPACE_TYPE_ENUM.PRIVATE) {
              privateSpaces.value.push({ id: space.id, spaceName: space.spaceName ?? '' })
            } else {
              teamSpaces.value.push({ id: space.id, spaceName: space.spaceName ?? '' })
            }
          }
        }
      }
    }
    // 查询作为成员加入的团队空间（补充非创建者的团队空间）
    const teamRes = await listMyTeamSpaceUsingPost()
    if (teamRes.data.code === 0 && teamRes.data.data) {
      for (const su of teamRes.data.data) {
        if (su.space && su.space.id && !seenIds.has(su.space.id)) {
          seenIds.add(su.space.id)
          teamSpaces.value.push({ id: su.space.id, spaceName: su.space.spaceName ?? '' })
        }
      }
    }
  } catch (e: any) {
    message.error('加载空间列表失败')
  }
  spaceLoading.value = false
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      selectedSpaceId.value = undefined
      fetchSpaces()
    }
  }
)

const handleSubmit = async () => {
  if (!selectedSpaceId.value) {
    message.warning('请选择目标空间')
    return
  }
  submitLoading.value = true
  try {
    const res = await addApprovalUsingPost({
      pictureId: props.pictureId,
      spaceId: selectedSpaceId.value,
    })
    if (res.data.code === 0) {
      message.success('申请已提交，请等待管理员审核')
      props.onSuccess?.()
      handleClose()
    } else {
      message.error(res.data.message || '提交失败')
    }
  } catch (e: any) {
    message.error('提交失败：' + e.message)
  }
  submitLoading.value = false
}

const handleClose = () => {
  emit('update:visible', false)
}
</script>
