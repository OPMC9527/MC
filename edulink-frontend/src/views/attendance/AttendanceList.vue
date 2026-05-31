<template>
    <div class="page-container">
        <el-card class="search-card">
            <el-form :model="query" label-width="80px">
                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="班级">
                            <el-select v-model="query.classId" placeholder="请选择班级" clearable filterable>
                                <el-option v-for="c in classOptions" :key="c.id" :label="c.className" :value="c.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="学生姓名">
                            <el-input v-model="query.studentName" placeholder="学生姓名" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="日期范围">
                            <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
                                            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"
                                            @change="handleDateChange" style="width:100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8" :lg="6">
                        <el-form-item label="状态">
                            <el-select v-model="query.status" clearable>
                                <el-option label="正常" value="PRESENT" />
                                <el-option label="迟到" value="LATE" />
                                <el-option label="缺勤" value="ABSENT" />
                                <el-option label="请假" value="LEAVE" />
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
                    <span class="title">考勤记录</span>
                    <el-button v-if="isTeacherOrAdmin" type="primary" plain @click="openCheckInDialog">打卡</el-button>
                </div>
            </template>
            <el-table :data="tableData" border stripe v-loading="loading">
                <el-table-column prop="studentName" label="学生姓名" />
                <el-table-column prop="studentNumber" label="学号" />
                <el-table-column prop="className" label="班级" />
                <el-table-column prop="attendanceDate" label="日期" />
                <el-table-column prop="status" label="状态">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" />
                <el-table-column label="操作" width="150" v-if="isTeacherOrAdmin">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="openEditDialog(row)">修改</el-button>
                        <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                           layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="loadData" />
        </el-card>

        <!-- 打卡对话框（仅教师/管理员） -->
        <el-dialog v-model="checkInVisible" title="打卡" width="500px">
            <el-form :model="checkInForm" :rules="checkInRules" ref="checkInFormRef" label-width="100px">
                <el-form-item label="学生" prop="studentId">
                    <el-select v-model="checkInForm.studentId" placeholder="请选择学生" clearable filterable>
                        <el-option v-for="stu in studentList" :key="stu.id" :label="stu.realName" :value="stu.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="日期" prop="attendanceDate">
                    <el-date-picker v-model="checkInForm.attendanceDate" type="date" placeholder="选择日期"
                                    value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select v-model="checkInForm.status">
                        <el-option label="正常" value="PRESENT" />
                        <el-option label="迟到" value="LATE" />
                        <el-option label="缺勤" value="ABSENT" />
                        <el-option label="请假" value="LEAVE" />
                    </el-select>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="checkInForm.remark" type="textarea" :rows="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="checkInVisible = false">取消</el-button>
                <el-button type="primary" @click="submitCheckIn">确定</el-button>
            </template>
        </el-dialog>

        <!-- 编辑对话框（仅教师/管理员） -->
        <el-dialog v-model="editVisible" title="修改考勤" width="500px">
            <el-form :model="editForm" label-width="100px">
                <el-form-item label="状态">
                    <el-select v-model="editForm.status">
                        <el-option label="正常" value="PRESENT" />
                        <el-option label="迟到" value="LATE" />
                        <el-option label="缺勤" value="ABSENT" />
                        <el-option label="请假" value="LEAVE" />
                    </el-select>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="editForm.remark" type="textarea" :rows="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="editVisible = false">取消</el-button>
                <el-button type="primary" @click="submitEdit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAttendanceList, checkIn, updateAttendance, deleteAttendance } from '@/api/attendance'
import { getStudentList } from '@/api/student'
import { useUserStore } from '@/store/user'
import { getClassList } from '@/api/class'

const userStore = useUserStore()
const role = userStore.role
const isTeacherOrAdmin = role === 'TEACHER' || role === 'ADMIN'



const query = reactive({
    pageNum: 1,
    pageSize: 10,
    classId: '',
    studentName: '',
    startDate: '',
    endDate: '',
    status: ''
})
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const classOptions = ref([])
const loadClassOptions = async () => {
    try {
        const res = await getClassList({ pageNum: 1, pageSize: 100 })
        if (res.code === 200) classOptions.value = res.data.rows || []
    } catch (error) {
        console.error('加载班级列表失败', error)
    }
}

// 学生列表（仅教师/管理员需要）
const studentList = ref([])
const loadStudentList = async () => {
    if (!isTeacherOrAdmin) return
    try {
        const res = await getStudentList({ pageNum: 1, pageSize: 100 })
        if (res.code === 200) studentList.value = res.data.rows || []   // 注意取 rows
    } catch (error) {
        console.error('加载学生列表失败', error)
    }
}

// 打卡相关
const checkInVisible = ref(false)
const checkInFormRef = ref()
const checkInForm = reactive({
    studentId: '',
    attendanceDate: '',
    status: 'PRESENT',
    remark: '',
    teacherId: 1
})
const checkInRules = {
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    attendanceDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 编辑相关
const editVisible = ref(false)
const editForm = reactive({
    id: null,
    status: '',
    remark: ''
})

// 加载考勤数据
const loadData = async () => {
    console.log('loadData 被调用，query:', query)
    loading.value = true
    try {
        const res = await getAttendanceList(query)
        console.log('请求结果:', res)
        if (res.code === 200) {
            tableData.value = res.data.rows || []
            total.value = res.data.total || 0
        }
    } catch (error) {
        console.error('请求失败:', error)
    } finally {
        loading.value = false
    }
}

// 日期范围处理
const handleDateChange = (val) => {
    if (val) {
        query.startDate = val[0]
        query.endDate = val[1]
    } else {
        query.startDate = ''
        query.endDate = ''
    }
}

// 重置查询
const resetQuery = () => {
    query.classId = ''
    query.studentName = ''
    query.startDate = ''
    query.endDate = ''
    query.status = ''
    dateRange.value = []
    loadData()
}

// 打卡
const openCheckInDialog = () => {
    checkInForm.studentId = ''
    checkInForm.attendanceDate = ''
    checkInForm.status = 'PRESENT'
    checkInForm.remark = ''
    checkInVisible.value = true
}
const submitCheckIn = async () => {
    await checkInFormRef.value.validate()
    const res = await checkIn(checkInForm)
    if (res.code === 200) {
        ElMessage.success('打卡成功')
        checkInVisible.value = false
        loadData()
    }
}

// 编辑
const openEditDialog = (row) => {
    editForm.id = row.id
    editForm.status = row.status
    editForm.remark = row.remark
    editVisible.value = true
}
const submitEdit = async () => {
    const res = await updateAttendance(editForm)
    if (res.code === 200) {
        ElMessage.success('修改成功')
        editVisible.value = false
        loadData()
    }
}

// 删除
const handleDelete = (id) => {
    ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }).then(async () => {
        await deleteAttendance(id)
        loadData()
    }).catch(() => {})
}



// 状态映射
const statusText = (s) => ({ PRESENT: '正常', LATE: '迟到', ABSENT: '缺勤', LEAVE: '请假' }[s] || s)
const statusType = (s) => ({ PRESENT: 'success', LATE: 'warning', ABSENT: 'danger', LEAVE: 'info' }[s] || 'info')

onMounted(() => {
    loadData()
    loadStudentList()
    loadClassOptions()
})
</script>