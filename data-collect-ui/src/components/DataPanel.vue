<template>
    <div class="data-panel">
        <div class="table-scroll">
            <el-table :data="pagedRows" height="100%" border stripe size="small"
                      @row-click="showDetail" highlight-current-row>
                <el-table-column prop="id" label="id" width="90" sortable>
                    <template #default="{row}"><span class="mono">{{ row.id }}</span></template>
                </el-table-column>
                <el-table-column prop="topic" label="topic" width="300" show-overflow-tooltip/>
                <el-table-column prop="len" label="长度" width="80" sortable>
                    <template #default="{row}"><span class="mono">{{ row.len }}</span></template>
                </el-table-column>
                <el-table-column label="解析结果 (JSON)" min-width="380">
                    <template #default="{row}">
                        <div class="json-cell" :title="jsonText(row.data)">{{ jsonText(row.data) }}</div>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 底部详情区：点行就地展示，不弹右侧抽屉 -->
        <div v-if="store.currentRow" class="detail-box">
            <p class="detail-meta">
                id: {{ store.currentRow.id }} · topic: {{ store.currentRow.topic }} · 长度: {{ store.currentRow.len }}
                <el-button size="mini" link style="margin-left:8px" @click="store.currentRow = null">关闭</el-button>
            </p>
            <pre class="detail-pre">{{ prettyJson(store.currentRow.data) }}</pre>
        </div>

        <div class="pager-row">
            <span class="stat-info">共 {{ filteredRows.length }} 条</span>
            <el-pagination layout="prev, pager, next, sizes" :total="filteredRows.length"
                           v-model:current-page="store.page" v-model:page-size="store.pageSize"
                           :page-sizes="[50, 100, 200, 500]" small background/>
        </div>
    </div>
</template>

<script setup>
import { store, pagedRows, filteredRows, jsonText, prettyJson, showDetail } from '../store'
</script>
