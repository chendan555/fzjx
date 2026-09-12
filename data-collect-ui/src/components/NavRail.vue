<template>
    <nav class="nav-rail">
        <div class="nav-brand">
            <span class="nav-brand-mark">DC</span>
        </div>
        <div class="nav-menu">
            <button v-for="p in PAGES" :key="p.key" class="nav-item"
                    :class="{active: store.currentPage === p.key}"
                    :title="p.label" @click="switchPage(p.key)">
                <span class="nav-icon" v-html="p.icon"></span>
                <span class="nav-label">{{ p.label }}</span>
            </button>
        </div>
        <div class="nav-foot"></div>
    </nav>
</template>

<script setup>
import { store, switchPage } from '../store'
import { PAGES } from '../pages'
</script>
