<template>
    <div class="page-container">
        <!-- 搜索卡片 -->
        <el-card class="search-card">
            <el-form :model="query" label-width="80px">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="用户名">
                            <el-input v-model="query.username" placeholder="用户名" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="真实姓名">
                            <el-input v-model="query.realName" placeholder="真实姓名" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="角色">
                            <el-select v-model="query.role" placeholder="请选择" clearable>
                                <el-option label="管理员" value="ADMIN" />
                                <el-option label="教师" value="TEACHER" />
                                <el-option label="学生" value="STUDENT" />
                                <el-option label="家长" value="PARENT" />
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

        <!-- 用户列表卡片 -->
        <el-card>
            <template #header>
                <div class="card-header">
                    <span class="title">用户列表</span>
                    <el-button type="primary" plain @click="openAddDialog">添加用户</el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="username" label="用户名" min-width="100" />
                <el-table-column prop="realName" label="真实姓名" min-width="100" />
                <el-table-column prop="role" label="角色" min-width="80">
                    <template #default="{ row }">
                        <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="phone" label="手机号" min-width="120" />
                <el-table-column prop="email" label="邮箱" min-width="150" />

                <el-table-column prop="status" label="状态" min-width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="createTime" label="注册时间" min-width="160">
                    <template #default="{ row }">
                        {{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '' }}
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
                        <el-button link type="warning" size="small" @click="handleResetPassword(row.id)">重置密码</el-button>
                        <el-button link type="info" size="small" @click="toggleStatus(row)">
                            {{ row.status === 1 ? '禁用' : '启用' }}
                        </el-button>
                        <el-button link type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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

        <!-- 添加/编辑用户对话框 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="用户名" prop="username" v-if="!form.id">
                    <el-input v-model="form.username" placeholder="4-20位字母/数字/下划线" />
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="form.realName" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.email" />
                </el-form-item>
                <el-form-item label="角色" prop="role" v-if="!form.id">
                    <el-select v-model="form.role" placeholder="请选择角色">
                        <el-option label="教师" value="TEACHER" />
                        <el-option label="学生" value="STUDENT" />
                        <el-option label="家长" value="PARENT" />
                    </el-select>
                </el-form-item>
                <el-form-item label="班级" v-if="form.role === 'STUDENT'">
                    <el-select
                        v-model="form.classId"
                        placeholder="请选择班级"
                        clearable
                        filterable
                        :loading="classLoading"
                    >
                        <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="初始密码" v-if="!form.id">
                    <el-input v-model="form.password" placeholder="默认为123456" />
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
import { getUserList, addUser, updateUser, deleteUser, resetPassword, toggleUserStatus } from '@/api/user'
import { getClassList } from '@/api/class'
import dayjs from 'dayjs'

const query = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', role: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({ id: null, username: '', realName: '', phone: '', email: '', role: 'STUDENT', classId: null, password: '' })
const formRef = ref()
const classOptions = ref([])
const classLoading = ref(false)

const roleLabel = (role) => ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生', PARENT: '家长' }[role] || role)
const roleTagType = (role) => ({ ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'success', PARENT: 'info' }[role] || 'info')

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名由4-20位字母、数字或下划线组成', trigger: 'blur' }
    ],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
    loading.value = true
    try {
        const res = await getUserList(query)
        if (res.code === 200) {
            tableData.value = res.data.rows || []
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}

const resetQuery = () => {
    Object.assign(query, { pageNum: 1, username: '', realName: '', role: '' })
    loadData()
}

const openAddDialog = () => {
    dialogTitle.value = '添加用户'
    Object.assign(form, { id: null, username: '', realName: '', phone: '', email: '', role: 'STUDENT', classId: null, password: '' })
    dialogVisible.value = true
}

const openEditDialog = (row) => {
    dialogTitle.value = '编辑用户'
    // 确保 classId 为数字
    const classId = row.classId ? Number(row.classId) : null
    Object.assign(form, {
        id: row.id,
        username: row.username,
        realName: row.realName,
        phone: row.phone,
        email: row.email,
        role: row.role,
        classId: classId,
        password: ''
    })
    dialogVisible.value = true
}

const submitForm = async () => {
    await formRef.value.validate()
    const data = { ...form }
    if (form.id) {
        delete data.password   // 编辑时不修改密码
        await updateUser(data)
        ElMessage.success('更新成功')
    } else {
        await addUser(data)
        ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
}

const handleResetPassword = (id) => {
    ElMessageBox.confirm('确认重置密码？重置后密码为123456', '提示', { type: 'warning' }).then(async () => {
        await resetPassword(id)
        ElMessage.success('密码已重置为123456')
    })
}

const toggleStatus = (row) => {
    const newStatus = row.status === 1 ? 0 : 1
    ElMessageBox.confirm(`确认${newStatus === 1 ? '启用' : '禁用'}该用户？`, '提示', { type: 'warning' }).then(async () => {
        await toggleUserStatus(row.id, newStatus)
        ElMessage.success('操作成功')
        loadData()
    })
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除？删除后不可恢复', '提示', { type: 'error' }).then(async () => {
        await deleteUser(id)
        ElMessage.success('删除成功')
        loadData()
    })
}

const loadClassOptions = async () => {
    classLoading.value = true
    try {
        const res = await getClassList({ pageNum: 1, pageSize: 100 })
        if (res.code === 200) {
            classOptions.value = res.data.rows || res.data
        }
    } finally {
        classLoading.value = false
    }
}

onMounted(() => {
    loadData()
    loadClassOptions()
})
</script>

<style scoped>
/* 复用全局样式 */
</style>