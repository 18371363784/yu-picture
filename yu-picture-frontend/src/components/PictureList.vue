<template>
  <div class="picture-list">
    <a-empty
      v-if="!loading && isEmpty"
      class="yx-empty-illustrated picture-list-empty"
    >
      <template #image>
        <div class="empty-illustration" aria-hidden="true">
          <svg viewBox="0 0 200 160" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="yxAuroraA" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #a5b4fc" />
                <stop offset="100%" style="stop-color: #7dd3fc" />
              </linearGradient>
              <linearGradient id="yxAuroraB" x1="100%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color: #e9d5ff" />
                <stop offset="100%" style="stop-color: #99f6e4" />
              </linearGradient>
            </defs>
            <ellipse cx="100" cy="128" rx="72" ry="14" fill="url(#yxAuroraB)" opacity="0.45" />
            <rect x="36" y="48" width="128" height="88" rx="14" fill="white" opacity="0.9" />
            <rect x="36" y="48" width="128" height="88" rx="14" fill="url(#yxAuroraA)" opacity="0.2" />
            <rect x="48" y="62" width="104" height="64" rx="8" fill="url(#yxAuroraA)" opacity="0.35" />
            <circle cx="76" cy="94" r="12" fill="white" opacity="0.95" />
            <path
              d="M118 82 L158 118 L78 118 Z"
              fill="url(#yxAuroraB)"
              opacity="0.55"
            />
            <circle cx="132" cy="72" r="6" fill="#6366f1" opacity="0.5" />
          </svg>
        </div>
      </template>
      <template #description>
        <span class="empty-desc-primary">暂无图片</span>
        <div class="empty-desc-sub">试试调整分类、标签或稍后再来看看</div>
      </template>
    </a-empty>

    <a-list
      v-else
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture, index }">
        <a-list-item style="padding: 0">
          <a-card hoverable class="picture-card" @click="doClickPicture(picture)">
            <template #cover>
              <div class="picture-cover-wrap">
                <div v-show="!isImageLoaded(picture, index)" class="picture-cover-skeleton">
                  <a-skeleton-image class="skeleton-block" active />
                </div>
                <img
                  v-show="isImageLoaded(picture, index)"
                  :alt="picture.name"
                  :src="coverSrc(picture)"
                  class="picture-cover-img"
                  loading="lazy"
                  @load="onImageLoad(picture, index)"
                />
              </div>
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <ShareAltOutlined @click="(e) => doShare(picture, e)" />
              <SearchOutlined @click="(e) => doSearch(picture, e)" />
              <EditOutlined v-if="canEdit" @click="(e) => doEdit(picture, e)" />
              <DeleteOutlined v-if="canDelete" @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink || ''" />
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { toAbsolutePictureUrl } from '@/utils'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const isEmpty = computed(() => !props.dataList?.length)

const imageLoaded = reactive<Record<string, boolean>>({})

function imageKey(picture: API.PictureVO, index: number) {
  return String(picture.id ?? `i-${index}`)
}

function isImageLoaded(picture: API.PictureVO, index: number) {
  return imageLoaded[imageKey(picture, index)] === true
}

function onImageLoad(picture: API.PictureVO, index: number) {
  imageLoaded[imageKey(picture, index)] = true
}

function coverSrc(picture: API.PictureVO) {
  return toAbsolutePictureUrl(picture.thumbnailUrl ?? picture.url) ?? ''
}

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 搜索
const doSearch = (picture: API.PictureVO, e: MouseEvent) => {
  e.stopPropagation()
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture: API.PictureVO, e: MouseEvent) => {
  e.stopPropagation()
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除数据
const doDelete = async (picture: API.PictureVO, e: MouseEvent) => {
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

const shareModalRef = ref()
const shareLink = ref<string>()
const doShare = (picture: API.PictureVO, e: MouseEvent) => {
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
.picture-list {
  min-height: 200px;
}

.picture-list-empty {
  padding: 48px 24px 32px;
  border-radius: var(--yx-radius-lg, 20px);
  background: rgba(255, 255, 255, 0.5);
  border: 1px dashed rgba(148, 163, 184, 0.35);
}

.empty-illustration {
  width: 200px;
  max-width: 100%;
  margin: 0 auto;
}

.empty-illustration svg {
  width: 100%;
  height: auto;
  display: block;
  filter: drop-shadow(0 12px 28px rgba(99, 102, 241, 0.15));
}

.empty-desc-primary {
  display: block;
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--yx-text-title, #0f172a);
  letter-spacing: 0.04em;
}

.empty-desc-sub {
  margin-top: 6px;
  font-size: 0.875rem;
  color: var(--yx-text-muted, #94a3b8);
  line-height: 1.6;
}

.picture-card {
  border-radius: 16px !important;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.06);
  transition:
    transform 0.28s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.28s ease;
}

.picture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(99, 102, 241, 0.14), 0 8px 20px rgba(15, 23, 42, 0.08);
}

.picture-cover-wrap {
  position: relative;
  height: 180px;
  background: linear-gradient(135deg, rgba(224, 231, 255, 0.6), rgba(204, 251, 241, 0.5));
}

.picture-cover-skeleton {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.picture-cover-skeleton :deep(.ant-skeleton-image) {
  width: 100%;
  height: 100%;
}

.skeleton-block {
  border-radius: 0 !important;
}

.picture-cover-img {
  position: relative;
  display: block;
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: opacity 0.35s ease;
}

.picture-card :deep(.ant-card-meta-title) {
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--yx-text-title, #0f172a);
}

.picture-card :deep(.ant-card-body) {
  padding: 12px 14px 8px;
}

.picture-list :deep(.ant-list-item) {
  margin-bottom: 4px;
}

.picture-list :deep(.ant-spin-nested-loading) {
  min-height: 120px;
}

.picture-list :deep(.ant-spin-container::after) {
  border-radius: var(--yx-radius, 14px);
}
</style>
