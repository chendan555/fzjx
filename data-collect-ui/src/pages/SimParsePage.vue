<template>
    <div class="sim-page">
        <div class="main-wrap">
            <LeftRail/>
            <div class="right-area">
                <DataWorkspace/>
                <DataPanel/>
            </div>
        </div>
    </div>
</template>

<script setup>
import LeftRail from '../components/LeftRail.vue'
import DataWorkspace from '../components/DataWorkspace.vue'
import DataPanel from '../components/DataPanel.vue'
</script>
