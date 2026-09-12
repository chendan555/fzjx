<template>
    <div class="page-header">
        <span class="wordmark">
            <span>DataCollect</span>
            <span class="mono-tag">解析平台</span>
        </span>
        <span class="conn-status" :class="{on: store.connected, off: !store.connected}">
            {{ store.connected ? '已连接 · ' + store.connInfo : '未连接' }}
        </span>
    </div>

    <div class="conn-panel">
        <el-input v-model="store.form.host" placeholder="127.0.0.1" size="small">
            <template #prepend>主机</template>
        </el-input>
        <el-input v-model="store.form.port" placeholder="3306" size="small" style="width:80px">
            <template #prepend>端口</template>
        </el-input>
        <el-input v-model="store.form.user" placeholder="root" size="small" style="width:90px">
            <template #prepend>用户</template>
        </el-input>
        <el-input v-model="store.form.pass" placeholder="密码" show-password size="small" style="width:90px">
            <template #prepend>密码</template>
        </el-input>
        <el-input v-model="store.form.db" placeholder="data_collect" size="small">
            <template #prepend>采集库</template>
        </el-input>
        <el-input v-model="store.form.modelDb" placeholder="modelmanager" size="small">
            <template #prepend>模型库</template>
        </el-input>
        <el-button v-if="!store.connected" type="primary" size="small" :loading="store.connecting" @click="connect">连接</el-button>
        <el-button v-else type="danger" size="small" plain @click="disconnect">断开</el-button>
        <span class="conn-hint">http://{{ localIp }}:8080 · 局域网用本机 IP</span>
        <span v-if="store.opStatus" class="stat-info">{{ store.opStatus }}</span>
    </div>
</template>

<script setup>
import { store, localIp, connect, disconnect } from '../store'
</script>
