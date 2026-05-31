<template>
    <div class="page-container">
        <!-- 搜索卡片 -->
        <el-card class="search-card">
            <el-form :model="query" label-width="80px" label-position="left">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="班级">
                            <el-input v-model="query.className" placeholder="班级名称" clearable/>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="姓名">
                            <el-input v-model="query.studentName" placeholder="学生姓名" clearable/>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="学号">
                            <el-input v-model="query.studentNumber" placeholder="学号" clearable />
                            <el-checkbox v-model="query.exactMatch" style="margin-left: 10px;">精确匹配</el-checkbox>
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

        <!-- 数据表格卡片 -->
        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="title">学生列表</span>
                    <el-button type="primary" plain size="small" @click="openAddDialog">
                        <el-icon>
                            <Plus/>
                        </el-icon>
                        添加学生
                    </el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="studentNumber" label="学号" min-width="120"/>
                <el-table-column prop="realName" label="姓名" min-width="100"/>
                <el-table-column prop="className" label="班级" min-width="120"/>
                <el-table-column prop="phone" label="手机号" min-width="130"/>
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" size="small" @click="openEditDialog(row)">
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
                <el-form-item label="姓名" prop="realName">
                    <el-input v-model="form.realName"/>
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone"/>
                </el-form-item>
                <el-form-item label="邮箱">
                    <el-input v-model="form.email"/>
                </el-form-item>
                <el-form-item label="学号">
                    <el-input v-model="query.studentNumber" placeholder="学号" clearable />
                </el-form-item>
                <el-form-item label="班级" prop="classId">
                    <el-select
                        v-model="form.classId"
                        placeholder="请选择或输入班级"
                        clearable
                        filterable
                        :loading="classLoading"
                    >
                        <el-option
                            v-for="item in classOptions"
                            :key="item.id"
                            :label="item.className"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="性别">
                    <el-select v-model="form.gender">
                        <el-option label="男" value="MALE"/>
                        <el-option label="女" value="FEMALE"/>
                    </el-select>
                </el-form-item>
                <el-form-item label="出生日期">
                    <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD"/>
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
import {reactive, ref, onMounted} from 'vue'
import {getClassList} from '@/api/class'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Edit, Delete} from '@element-plus/icons-vue'
import {getStudentList, addStudent, updateStudent, deleteStudent} from '@/api/student'

const query = reactive({pageNum: 1, pageSize: 10, className: '', studentName: '', studentNumber: '',exactMatch: false})
const tableData = ref([])
const total = ref(0)
const classOptions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const classLoading = ref(false)
const form = reactive({
    id: null,
    realName: '',
    phone: '',
    email: '',
    studentNumber: '',
    classId: '',
    gender: 'MALE',
    birthDate: ''
})
const formRef = ref()
const rules = {realName: [{required: true, message: '请输入姓名'}], phone: [{required: true, message: '请输入手机号'}]}

const loadData = async () => {
    loading.value = true
    try {
        const res = await getStudentList(query)
        if (res.code === 200) {
            tableData.value = res.data.rows || []
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}
const resetQuery = () => {
    Object.assign(query, {pageNum: 1, className: '', studentName: '', studentNumber: ''});
    loadData()
}
const openAddDialog = () => {
    dialogTitle.value = '添加学生'
    Object.assign(form, {
        id: null,
        realName: '',
        phone: '',
        email: '',
        studentNumber: '',
        classId: '',
        gender: 'MALE',
        birthDate: ''
    })
    dialogVisible.value = true
}
const openEditDialog = (row) => {
    dialogTitle.value = '编辑学生'
    Object.assign(form, row)
    dialogVisible.value = true
}
const submitForm = async () => {
    await formRef.value.validate()
    const res = form.id ? await updateStudent(form) : await addStudent(form)
    if (res.code === 200) {
        ElMessage.success('操作成功')
        dialogVisible.value = false
        loadData()
    }
}
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除？', '提示', {type: 'warning'}).then(async () => {
        const res = await deleteStudent(id)
        if (res.code === 200) {
            ElMessage.success('删除成功');
            loadData()
        }
    })
}
const loadClassOptions = async () => {
    classLoading.value = true
    try {
        const res = await getClassList()
        if (res.code === 200) classOptions.value = res.data
    } finally {
        classLoading.value = false
    }
}

onMounted(() => {
    loadData()
    loadClassOptions()
})
</script>