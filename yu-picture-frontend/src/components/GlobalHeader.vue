<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">秒寻图仓</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示栏 -->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item>
                    <router-link to="/my_approvals">
                      <FileTextOutlined />
                      我的审批
                    </router-link>
                  </a-menu-item>
                  <a-menu-item>
                    <router-link to="/user_exchange_vip">
                      <GiftOutlined />
                      兑换会员
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { FileTextOutlined, GiftOutlined, HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()

// 未经过滤的菜单项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: '/admin/approvalManage',
    label: '审批管理',
    title: '审批管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '资料社区'),
    title: '资料社区',
  },
]

// 根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const key = menu?.key
    if (typeof key === 'string' && key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const items = computed(() => filterMenus(originItems))

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#globalHeader {
  max-width: 1400px;
  margin: 0 auto;
}

#globalHeader :deep(.ant-menu-horizontal) {
  border-bottom: none;
  background: transparent;
  line-height: 48px;
}

#globalHeader :deep(.ant-menu-item) {
  border-radius: 10px !important;
  margin-inline: 4px;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

#globalHeader :deep(.ant-menu-item:hover) {
  background: rgba(99, 102, 241, 0.08) !important;
  color: var(--yx-primary, #6366f1);
}

#globalHeader :deep(.ant-menu-item-selected) {
  background: rgba(99, 102, 241, 0.12) !important;
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
  padding: 4px 0;
  border-radius: 12px;
  transition: opacity 0.2s ease;
}

#globalHeader .title-bar:hover {
  opacity: 0.88;
}

.title {
  color: var(--yx-text-title, #0f172a);
  font-size: 1.125rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-left: 12px;
}

.logo {
  height: 44px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.15);
}

.user-login-status {
  text-align: right;
}

.user-login-status :deep(.ant-space) {
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 999px;
  transition:
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.user-login-status :deep(.ant-space:hover) {
  background: rgba(99, 102, 241, 0.08);
  box-shadow: 0 2px 12px rgba(99, 102, 241, 0.1);
}
</style>
