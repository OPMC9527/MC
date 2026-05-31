<template>
    <div class="page-container">
        <!-- 搜索卡片 -->
        <el-card class="search-card">
            <el-form :inline="true" :model="query">
                <el-form-item label="班级名称">
                    <el-input v-model="query.className" placeholder="班级名称" clearable />
                </el-form-item>
                <el-form-item label="年级">
                    <el-select v-model="query.grade" placeholder="请选择" clearable>
                        <el-option label="2024级" value="2024级" />
                        <el-option label="2025级" value="2025级" />
                        <el-option label="2026级" value="2026级" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadData">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="title">班级列表</span>
                    <el-button type="primary" plain @click="openAddDialog">添加班级</el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="className" label="班级名称" min-width="150" />
                <el-table-column prop="grade" label="年级" min-width="100" />
                <el-table-column prop="headTeacherName" label="班主任" min-width="120" />
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                        <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                v-model:current-page="query.pageNum"
                v-model:page-size="query.pageSize"
                :total="total"
                layout="total, sizes, prev, pager, next"
                @current-change="loadData"
                @size-change="loadData"
            />
        </el-card>

        <!-- 添加/编辑对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="班级名称" prop="className">
                    <el-input v-model="form.className" placeholder="请输入班级名称" />
                </el-form-item>
                <el-form-item label="年级" prop="grade">
                    <el-select v-model="form.grade" placeholder="请选择年级" clearable>
                        <el-option label="2024级" value="2024级" />
                        <el-option label="2025级" value="2025级" />
                        <el-option label="2026级" value="2026级" />
                    </el-select>
                </el-form-item>
                <el-form-item label="班主任" prop="headTeacherId">
                    <el-select v-model="form.headTeacherId" placeholder="请选择班主任" clearable filterable>
                        <el-option v-for="t in teacherList" :key="t.id" :label="t.realName" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="form.description" type="textarea" :rows="3" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitForm">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassList, addClass, updateClass, deleteClass } from '@/api/class'
import { getTeacherList } from '@/api/user'

const query = reactive({ pageNum: 1, pageSize: 10, className: '', grade: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({ id: null, className: '', grade: '', headTeacherId: null, description: '' })
const formRef = ref()
const teacherList = ref([])

const rules = {
    className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
    grade: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const loadData = async () => {
    loading.value = true
    try {
        const res = await getClassList(query)
        if (res.code === 200) {
            // 后端返回的数据结构为 { total, rows }
            tableData.value = res.data.rows || []   // 提取数组
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}
const resetQuery = () => {
    Object.assign(query, { pageNum: 1, className: '', grade: '' })
    loadData()
}

const loadTeacherList = async () => {
    const res = await getTeacherList()
    if (res.code === 200) teacherList.value = res.data
}

const openAddDialog = () => {
    dialogTitle.value = '添加班级'
    Object.assign(form, { id: null, className: '', grade: '', headTeacherId: null, description: '' })
    dialogVisible.value = true
}

const openEditDialog = (row) => {
    dialogTitle.value = '编辑班级'
    Object.assign(form, row)
    dialogVisible.value = true
}

const submitForm = async () => {
    await formRef.value.validate()
    const res = form.id ? await updateClass(form) : await addClass(form)
    if (res.code === 200) {
        ElMessage.success('操作成功')
        dialogVisible.value = false
        loadData()
    }
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除该班级？删除后无法恢复', '提示', { type: 'warning' }).then(async () => {
        await deleteClass(id)
        loadData()
    })
}

onMounted(() => {
    loadData()
    loadTeacherList()
})
</script>