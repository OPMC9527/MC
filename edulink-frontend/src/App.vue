<!-- src/App.vue -->
<template>
    <router-view/>
</template>

<script setup>
</script>

<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html, body, #app {
    height: 100%;
    width: 100%;
}

body {
    font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 表格行高 */
.el-table .el-table__row {
    height: 48px;
}

.el-table .el-table__cell {
    padding: 8px 0;
}
</style>