<template>
    <div class="fx-frame-wrap">
        <iframe
            class="fx-frame"
            :src="frameSrc"
            title="弹道特征点提取"
            loading="eager"
        ></iframe>
    </div>
</template>

<script setup>
/**
 * 弹道特征点提取
 * 自包含工具页（独立 HTML + tokens），经 public/feature-extract/ 原样承载，
 * 以 iframe 嵌入应用宿主。菜单切换即显示本页面。
 */
import { computed } from 'vue'

const frameSrc = computed(() => import.meta.env.BASE_URL + 'feature-extract/index.html?v=' + Date.now())
</script>

<style scoped>
.fx-frame-wrap {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    background: var(--color-paper);
}
.fx-frame {
    flex: 1;
    min-height: 0;
    width: 100%;
    border: 0;
}
</style>
