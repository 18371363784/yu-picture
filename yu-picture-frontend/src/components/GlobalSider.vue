<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      width="200"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import { FileTextOutlined, GiftOutlined, IdcardOutlined, PictureOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

// 固定的菜单列表
const fixedMenuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/user/profile',
    label: '个人主页',
    icon: () => h(IdcardOutlined),
  },
  {
    key: '/my_approvals',
    label: '我的审批',
    icon: () => h(FileTextOutlined),
  },
  {
    key: '/user_exchange_vip',
    label: '兑换会员',
    icon: () => h(GiftOutlined),
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])
const menuItems = computed(() => {
  // 如果用户没有团队空间，则只展示固定菜单
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems
  }
  // 如果用户有团队空间，则展示固定菜单和团队空间菜单
  // 展示团队空间分组
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}

#globalSider :deep(.ant-menu-inline) {
  background: transparent !important;
  border-inline-end: none !important;
}

#globalSider :deep(.ant-menu-item) {
  border-radius: 10px !important;
  margin-block: 4px;
  width: calc(100% - 8px);
  margin-inline: 4px;
  transition:
    background 0.22s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

#globalSider :deep(.ant-menu-item:hover) {
  background: rgba(99, 102, 241, 0.1) !important;
  transform: translateX(2px);
}

#globalSider :deep(.ant-menu-item-selected) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.18), rgba(14, 165, 233, 0.1)) !important;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.12);
}

#globalSider :deep(.ant-menu-item-group-title) {
  padding-inline: 16px !important;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: var(--yx-text-muted, #94a3b8);
  text-transform: uppercase;
}
</style>
