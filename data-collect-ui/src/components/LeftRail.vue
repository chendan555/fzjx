<template>
    <div class="left-panel">
        <div class="left-section">
            <p class="section-head">
                <span>数据表 <span class="count">({{ store.tables.length }})</span></span>
                <el-button size="mini" link type="primary" @click="refreshTables">刷新</el-button>
            </p>
            <el-input v-model="store.tableKeyword" placeholder="搜索表名 / 任务ID" size="small" clearable style="margin-bottom:8px"/>
            <div class="table-list">
                <div v-for="t in filteredTables" :key="t.name" class="table-item"
                     :class="{active: store.selectedTable === t.name}" @click="selectTable(t.name)">
                    <span>{{ t.name }}</span>
                </div>
                <el-empty v-if="filteredTables.length === 0" description="没有匹配的表" :image-size="52"/>
            </div>
        </div>
        <div class="left-section">
            <p class="section-head">
                <span>主题 <span class="count">({{ store.topics.length }})</span></span>
                <span>
                    <el-button size="mini" link type="primary" @click="checkAll">全选</el-button>
                    <el-button size="mini" link @click="uncheckAll">清空</el-button>
                </span>
            </p>
            <el-input v-model="store.topicKeyword" placeholder="搜索主题 / 备注" size="small" clearable style="margin-bottom:8px"/>
            <div class="topic-list">
                <div v-for="t in filteredTopics" :key="t.name" class="topic-item">
                    <el-checkbox v-model="t.checked">{{ t.name }}</el-checkbox>
                    <span v-if="t.remark" class="remark">{{ t.remark }}</span>
                </div>
                <el-empty v-if="filteredTopics.length === 0" description="没有匹配的主题" :image-size="52"/>
            </div>
        </div>
    </div>
</template>

<script setup>
import { store, filteredTables, filteredTopics, selectTable, checkAll, uncheckAll, refreshTables } from '../store'
</script>
