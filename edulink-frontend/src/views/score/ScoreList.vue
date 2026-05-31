<template>
    <div class="page-container">
        <el-card class="search-card">
            <el-form :model="query" label-width="80px">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="学生姓名">
                            <el-input v-model="query.studentName" placeholder="学生姓名" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="班级">
                            <el-input v-model="query.className" placeholder="班级名称" clearable />
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
                    <span class="title">成绩列表</span>
                    <el-button v-if="isTeacherOrAdmin" type="primary" plain @click="openAddDialog">录入成绩</el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="studentName" label="学生姓名" />
                <el-table-column prop="className" label="班级" />
                <el-table-column prop="courseName" label="课程" />
                <el-table-column prop="score" label="分数" />
                <el-table-column prop="examType" label="考试类型" />
                <el-table-column prop="examDate" label="考试日期" />
                <el-table-column label="操作" width="180" v-if="isTeacherOrAdmin">
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

        <!-- 录入/编辑成绩对话框（仅教师/管理员） -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="学生" prop="studentId">
                    <el-select v-model="form.studentId" placeholder="请选择学生" clearable filterable>
                        <el-option v-for="stu in studentOptions" :key="stu.id" :label="stu.realName" :value="stu.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="课程" prop="courseName">
                    <el-select v-model="form.courseName" placeholder="请选择课程" clearable filterable>
                        <el-option label="Java程序设计" value="Java程序设计" />
                        <el-option label="数据库原理" value="数据库原理" />
                        <el-option label="Web前端开发" value="Web前端开发" />
                        <el-option label="软件工程" value="软件工程" />
                        <el-option label="数据结构" value="数据结构" />
                        <el-option label="计算机网络" value="计算机网络" />
                    </el-select>
                </el-form-item>
                <el-form-item label="分数" prop="score">
                    <el-input-number v-model="form.score" :min="0" :max="100" />
                </el-form-item>
                <el-form-item label="考试类型" prop="examType">
                    <el-select v-model="form.examType" placeholder="请选择考试类型" clearable>
                        <el-option label="期中考试" value="期中考试" />
                        <el-option label="期末考试" value="期末考试" />
                        <el-option label="单元测试" value="单元测试" />
                        <el-option label="平时作业" value="平时作业" />
                    </el-select>
                </el-form-item>
                <el-form-item label="考试日期" prop="examDate">
                    <el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
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
import { getScoreList, addScore, updateScore, deleteScore } from '@/api/score'
import { getStudentList } from '@/api/student'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const role = userStore.role
const isTeacherOrAdmin = role === 'TEACHER' || role === 'ADMIN'

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    studentName: '',
    className: ''
})
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 学生列表（仅教师/管理员需要，用于成绩录入下拉框）
const studentOptions = ref([])
const loadStudentOptions = async () => {
    if (!isTeacherOrAdmin) return
    try {
        const res = await getStudentList({ pageNum: 1, pageSize: 100 })
        if (res.code === 200) {
            // 取 rows 数组
            studentOptions.value = res.data.rows || []
        }
    } catch (error) {
        console.error('加载学生列表失败', error)
    }
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
    id: null,
    studentId: '',
    courseName: '',
    score: 0,
    examType: '',
    examDate: ''
})
const formRef = ref()
const rules = {
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    courseName: [{ required: true, message: '请填写课程名称', trigger: 'blur' }],
    score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
    examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
    examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }]
}

// 加载成绩数据
const loadData = async () => {
    loading.value = true
    try {
        const res = await getScoreList(query)
        if (res.code === 200) {
            tableData.value = res.data.rows || []
            total.value = res.data.total || 0
        }
    } finally {
        loading.value = false
    }
}

// 重置查询
const resetQuery = () => {
    query.studentName = ''
    query.className = ''
    loadData()
}

// 打开录入对话框
const openAddDialog = () => {
    dialogTitle.value = '录入成绩'
    form.id = null
    form.studentId = ''
    form.courseName = ''
    form.score = 0
    form.examType = ''
    form.examDate = ''
    dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
    dialogTitle.value = '编辑成绩'
    Object.assign(form, row)
    dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
    await formRef.value.validate()
    const res = form.id ? await updateScore(form) : await addScore(form)
    if (res.code === 200) {
        ElMessage.success('操作成功')
        dialogVisible.value = false
        loadData()
    }
}

// 删除
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除该条成绩记录吗？', '提示', { type: 'warning' }).then(async () => {
        try {
            const res = await deleteScore(id)
            if (res.code === 200) {
                ElMessage.success(res.message || '删除成功')
                loadData()   // 刷新列表
            } else {
                ElMessage.error(res.message || '删除失败')
            }
        } catch (error) {
            console.error('删除失败', error)
            ElMessage.error('网络错误或服务器异常')
        }
    }).catch(() => {})
}

onMounted(() => {
    loadData()
    loadStudentOptions()
})
</script>

<style scoped>
.page-container {
    padding: 20px;
}
.search-card {
    margin-bottom: 20px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.title {
    border-left: 4px solid var(--el-color-primary);
    padding-left: 12px;
}
</style>