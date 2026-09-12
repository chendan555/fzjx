<template>
    <div id="app-wrap">
        <NavRail/>
        <div class="app-right">
            <ConnPanel/>
            <div class="page-host">
                <component :is="currentPage.component"/>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import NavRail from './components/NavRail.vue'
import ConnPanel from './components/ConnPanel.vue'
import { store } from './store'
import { getPage } from './pages'

const currentPage = computed(() => getPage(store.currentPage))
</script>
