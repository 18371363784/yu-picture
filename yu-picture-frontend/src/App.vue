<template>
  <div id="app">
    <a-config-provider :locale="zhCN" :theme="antdTheme">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { theme } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')

import BasicLayout from '@/layouts/BasicLayout.vue'

const antdTheme: ThemeConfig = {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#6366f1',
    colorInfo: '#0ea5e9',
    borderRadius: 12,
    fontFamily:
      "'Plus Jakarta Sans', 'Noto Sans SC', system-ui, -apple-system, BlinkMacSystemFont, sans-serif",
  },
  components: {
    Button: {
      controlHeight: 38,
      fontWeight: 500,
    },
    Card: {
      borderRadiusLG: 16,
    },
    Input: {
      borderRadius: 10,
    },
    Menu: {
      borderRadius: 10,
      itemBorderRadius: 8,
    },
  },
}
</script>

<style scoped></style>
