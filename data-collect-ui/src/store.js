/** 全局状态 + 动作（响应式 store，组件直接 import 使用） */
import { reactive, computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from './api'

export const store = reactive({
    // 页面
    currentPage: 'sim-parse',
    // 连接
    form: { host: '127.0.0.1', port: '3306', user: 'root', pass: 'root', db: 'data_collect', modelDb: 'modelmanager' },
    connected: false,
    connecting: false,
    connInfo: '',
    opStatus: '',
    // 表 + 主题
    tables: [],
    tableKeyword: '',
    selectedTable: '',
    topics: [],
    topicKeyword: '',
    // 解析
    filterTopic: '全部主题',
    limit: 1000,
    rows: [],
    page: 1,
    pageSize: 100,
    parsing: false,
    statText: '',
    // 详情
    currentRow: null
})

export function switchPage(key) { store.currentPage = key }

export const localIp = ref(window.location.hostname || '127.0.0.1')

export const filteredTables = computed(() => {
    const k = store.tableKeyword.trim().toLowerCase()
    if (!k) return store.tables
    return store.tables.filter(t => t.name.toLowerCase().includes(k) || t.name.substring(1).includes(k))
})

export const filteredTopics = computed(() => {
    const k = store.topicKeyword.trim().toLowerCase()
    if (!k) return store.topics
    return store.topics.filter(t =>
        (t.name || '').toLowerCase().includes(k) || (t.remark || '').toLowerCase().includes(k))
})

export const checkedTopics = computed(() => store.topics.filter(t => t.checked))

export const filteredRows = computed(() => {
    if (!store.filterTopic || store.filterTopic === '全部主题') return store.rows
    return store.rows.filter(r => r.topic === store.filterTopic)
})

export const pagedRows = computed(() => {
    const start = (store.page - 1) * store.pageSize
    return filteredRows.value.slice(start, start + store.pageSize)
})

export async function connect() {
    store.connecting = true
    store.opStatus = '连接中...'
    try {
        const r = await api.connect(store.form)
        if (r.ok) {
            store.connected = true
            store.connInfo = `${store.form.db}@${store.form.host}:${store.form.port} / 模型库 ${store.form.modelDb}`
            await loadTables()
            await loadTopics()
            store.opStatus = `已连接 · ${store.tables.length} 张表 · ${store.topics.length} 个主题`
        } else {
            store.opStatus = '连接失败'
            ElMessage.error(r.message || '连接失败')
        }
    } catch (e) {
        store.opStatus = '连接异常'
        ElMessage.error('连接异常: ' + e.message)
    } finally {
        store.connecting = false
    }
}

export async function disconnect() {
    try { await api.disconnect() } catch (e) { /* 静默 */ }
    store.connected = false
    store.connInfo = ''
    store.opStatus = ''
    store.tables = []
    store.topics = []
    store.rows = []
    store.selectedTable = ''
}

export async function loadTables() {
    try {
        const r = await api.getTables()
        if (r.ok) {
            store.tables = r.tables
            if (store.tables.length > 0) store.selectedTable = store.tables[0].name
        } else {
            ElMessage.error('加载表失败: ' + r.message)
        }
    } catch (e) {
        ElMessage.error('加载表异常: ' + e.message)
    }
}

export async function loadTopics() {
    try {
        const r = await api.getTopics()
        if (r.ok) {
            store.topics = (r.topics || []).map(t => ({ ...t, checked: true }))
        } else {
            ElMessage.error('加载主题失败: ' + r.message)
        }
    } catch (e) {
        ElMessage.error('加载主题异常: ' + e.message)
    }
}

export function checkAll() { store.topics.forEach(t => t.checked = true) }
export function uncheckAll() { store.topics.forEach(t => t.checked = false) }
export function selectTable(name) { store.selectedTable = name }

export async function refreshTables() {
    if (!store.connected) return
    await loadTables()
}

export async function parseData() {
    const names = checkedTopics.value.map(t => t.name)
    if (names.length === 0) { ElMessage.warning('请至少勾选一个主题'); return }
    store.parsing = true
    store.statText = '解析中...'
    try {
        const r = await api.parseData(store.selectedTable, names, store.limit)
        if (r.ok) {
            const res = r.result
            store.rows = res.rows || []
            store.page = 1
            store.statText = `解析完成：成功 ${res.ok} 条，失败 ${res.fail} 条`
        } else {
            store.statText = '解析失败'
            ElMessage.error('解析失败: ' + r.message)
        }
    } catch (e) {
        store.statText = '解析异常'
        ElMessage.error('解析异常: ' + e.message)
    } finally {
        store.parsing = false
    }
}

export function jsonText(data) {
    if (typeof data === 'string') return data
    try { return JSON.stringify(data) } catch (e) { return String(data) }
}

export function prettyJson(data) {
    if (typeof data === 'string') {
        try { return JSON.stringify(JSON.parse(data), null, 2) } catch (e) { return data }
    }
    try { return JSON.stringify(data, null, 2) } catch (e) { return String(data) }
}

export function showDetail(row) {
    store.currentRow = row
}

export function exportJson() {
    const out = {
        table: store.selectedTable,
        filterTopic: store.filterTopic || '全部主题',
        rows: filteredRows.value
    }
    const blob = new Blob([JSON.stringify(out, null, 2)], { type: 'application/json' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = `data_collect_${store.selectedTable}_${Date.now()}.json`
    a.click()
    URL.revokeObjectURL(a.href)
    ElMessage.success(`已导出 ${filteredRows.value.length} 条`)
}
