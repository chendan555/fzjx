<template>
    <div class="right-panel">
        <div class="toolbar">
            <el-select v-model="store.filterTopic" filterable placeholder="主题筛选（可输入搜索）" clearable size="small">
                <el-option label="全部主题" value="全部主题"/>
                <el-option v-for="t in store.topics" :key="t.name"
                           :label="t.name + (t.remark ? '（' + t.remark + '）' : '')" :value="t.name"/>
            </el-select>
            <el-button type="primary" size="small" :loading="store.parsing"
                       :disabled="!store.connected || !store.selectedTable" @click="parseData">解析数据</el-button>
            <span class="stat-info">最大条数</span>
            <el-input-number v-model="store.limit" :min="1" :max="1000000" :step="100" size="small"/>
            <el-button size="small" :disabled="store.rows.length === 0" @click="exportJson">导出 JSON</el-button>
            <span class="stat-info">{{ store.statText }}</span>
        </div>
    </div>
</template>

<script setup>
import { store, parseData, exportJson } from '../store'
</script>
