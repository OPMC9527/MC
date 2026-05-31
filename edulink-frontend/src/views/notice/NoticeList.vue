<template>
    <div class="page-container">
        <el-card class="search-card">
            <el-form :model="query" label-width="80px">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="标题">
                            <el-input v-model="query.title" placeholder="标题" clearable/>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="紧急">
                            <el-select v-model="query.isUrgent" clearable>
                                <el-option label="紧急" :value="1"/>
                                <el-option label="普通" :value="0"/>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="8" :lg="6">
                        <el-form-item>
                            <el-button type="primary" @click="loadData">查询</el-button>
                            <el-button @click="resetQuery">重置</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
        </el-card>

        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="title">通知公告</span>
                    <el-button type="primary" plain size="small" @click="openPublishDialog">
                        <el-icon>
                            <Plus/>
                        </el-icon>
                        发布通知
                    </el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="title" label="标题" min-width="200"/>
                <el-table-column prop="publisherName" label="发布人" min-width="100"/>
                <el-table-column prop="createTime" label="发布时间" min-width="160"/>
                <el-table-column prop="isUrgent" label="紧急" min-width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.isUrgent ? 'danger' : 'info'" size="small">{{
                            row.isUrgent ? '紧急' : '普通'
                            }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" size="small" @click="viewDetail(row.id)">
                            <el-icon>
                                <View/>
                            </el-icon>
                            查看
                        </el-button>
                        <el-button link type="warning" size="small" @click="openEditDialog(row)">
                            <el-icon>
                                <Edit/>
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button link type="danger" size="small" @click="handleDelete(row.id)">
                            <el-icon>
                                <Delete/>
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                           layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="loadData"/>
        </el-card>

        <!-- 发布/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
            <el-form :model="form" label-width="100px">
                <el-form-item label="标题">
                    <el-input v-model="form.title"/>
                </el-form-item>
                <el-form-item label="内容">
                    <el-input v-model="form.content" type="textarea" :rows="5"/>
                </el-form-item>
                <el-form-item label="目标角色">
                    <el-select v-model="form.targetRoles" multiple placeholder="请选择">
                        <el-option label="学生" value="STUDENT"/>
                        <el-option label="教师" value="TEACHER"/>
                        <el-option label="家长" value="PARENT"/>
                    </el-select>
                </el-form-item>
                <el-form-item label="紧急">
                    <el-switch v-model="form.isUrgent" :active-value="1" :inactive-value="0"/>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitForm">确定</el-button>
            </template>
        </el-dialog>

        <!-- 详情对话框 -->
        <el-dialog v-model="detailVisible" title="通知详情" width="600px">
            <div v-if="detail">
                <h3>{{ detail.title }}</h3>
                <p>{{ detail.content }}</p>
                <div>发布人：{{ detail.publisherName }} &nbsp; 时间：{{ detail.createTime }}</div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import {reactive, ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, View, Edit, Delete} from '@element-plus/icons-vue'
import {getNoticeList, getNoticeDetail, publishNotice, updateNotice, deleteNotice} from '@/api/notice'
import {getScoreList} from "@/api/score.js";

const query = reactive({pageNum: 1, pageSize: 10, title: '', isUrgent: null})
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({id: null, title: '', content: '', targetRoles: [], isUrgent: 0})
const detailVisible = ref(false)
const detail = ref({})

const loadData = async () => {
    loading.value = true
    try {
        const res = await getNoticeList(query)
        if (res.code === 200) {
            tableData.value = res.data.rows || []
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}
const resetQuery = () => {
    Object.assign(query, {pageNum: 1, title: '', isUrgent: null});
    loadData()
}
const openPublishDialog = () => {
    dialogTitle.value = '发布通知'
    Object.assign(form, {id: null, title: '', content: '', targetRoles: [], isUrgent: 0})
    dialogVisible.value = true
}
const openEditDialog = (row) => {
    dialogTitle.value = '编辑通知'
    Object.assign(form, row)
    dialogVisible.value = true
}
const submitForm = async () => {
    const payload = {...form, targetRoles: JSON.stringify(form.targetRoles)}
    const res = form.id ? await updateNotice(payload) : await publishNotice(payload)
    if (res.code === 200) {
        ElMessage.success('操作成功');
        dialogVisible.value = false;
        loadData()
    }
}
const viewDetail = async (id) => {
    const res = await getNoticeDetail(id)
    if (res.code === 200) {
        detail.value = res.data;
        detailVisible.value = true
    }
}
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除？').then(async () => {
        await deleteNotice(id)
        loadData()
    })
}
onMounted(loadData)
</script>