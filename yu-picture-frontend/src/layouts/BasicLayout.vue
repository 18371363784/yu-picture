<template>
  <div id="basicLayout">
    <a-layout class="yx-layout-root">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout class="yx-layout-body">
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <div class="content-inner">
            <router-view />
          </div>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<style scoped>
.yx-layout-root {
  min-height: 100vh;
  background: transparent;
}

#basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 0;
  height: auto;
  line-height: unset;
  padding-block: 10px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.06);
}

#basicLayout .yx-layout-body {
  background: transparent;
}

#basicLayout .sider {
  background: rgba(255, 255, 255, 0.4) !important;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-right: 1px solid rgba(148, 163, 184, 0.18) !important;
  padding-top: 20px;
}

#basicLayout :deep(.ant-layout-sider-children) {
  background: transparent;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
  background: transparent !important;
}

#basicLayout .content {
  padding: 24px 28px 40px;
  background: transparent;
  margin-bottom: 0;
}

.content-inner {
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
}
</style>
