<template>
    <div class="page-container">
        <!-- 管理员/教师视图 -->
        <template v-if="role === 'ADMIN' || role === 'TEACHER'">
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-card class="stat-card">
                        <div class="stat-icon blue"><el-icon><User /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ statistics.studentCount || 0 }}</div>
                            <div class="stat-label">学生总数</div>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="stat-card">
                        <div class="stat-icon green"><el-icon><Avatar /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ statistics.teacherCount || 0 }}</div>
                            <div class="stat-label">教师人数</div>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="stat-card">
                        <div class="stat-icon orange"><el-icon><Document /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ statistics.avgScore || 0 }}</div>
                            <div class="stat-label">平均成绩</div>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="stat-card">
                        <div class="stat-icon purple"><el-icon><Bell /></el-icon></div>
                        <div class="stat-info">
                            <div class="stat-value">{{ statistics.todayNoticeCount || 0 }}</div>
                            <div class="stat-label">今日通知</div>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </template>

        <!-- 学生/家长视图 -->
        <template v-else>
            <!-- 学生视图（直接使用 statistics 中的成绩和考勤） -->
            <template v-if="role === 'STUDENT'">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-card>
                            <template #header>最近成绩</template>
                            <el-table :data="statistics.recentScores || []" border>
                                <el-table-column prop="courseName" label="课程" />
                                <el-table-column prop="score" label="分数" />
                                <el-table-column prop="examDate" label="考试日期" />
                            </el-table>
                        </el-card>
                    </el-col>
                    <el-col :span="12">
                        <el-card>
                            <template #header>考勤统计（近30天）</template>
                            <div v-if="statistics.attendanceStat">
                                <p>正常: {{ statistics.attendanceStat.presentDays || 0 }} 天</p>
                                <p>迟到: {{ statistics.attendanceStat.lateDays || 0 }} 天</p>
                                <p>缺勤: {{ statistics.attendanceStat.absentDays || 0 }} 天</p>
                                <p>请假: {{ statistics.attendanceStat.leaveDays || 0 }} 天</p>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </template>

            <!-- 家长视图（保持原有逻辑） -->
            <template v-else-if="role === 'PARENT'">
                <!-- 家长专用：孩子选择下拉框 -->
                <el-row :gutter="20" style="margin-bottom: 20px;">
                    <el-col :span="12">
                        <el-form-item label="查看孩子成绩">
                            <el-select
                                v-model="selectedChildId"
                                placeholder="请选择孩子"
                                clearable
                                filterable
                                @change="handleChildChange"
                            >
                                <el-option
                                    v-for="child in statistics.children"
                                    :key="child.id"
                                    :label="child.name"
                                    :value="child.id"
                                />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>

                <!-- 最近成绩（根据所选孩子动态展示） -->
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-card>
                            <template #header>最近成绩</template>
                            <el-table :data="childScores" border v-loading="scoreLoading">
                                <el-table-column prop="courseName" label="课程" />
                                <el-table-column prop="score" label="分数" />
                                <el-table-column prop="examDate" label="考试日期" />
                            </el-table>
                        </el-card>
                    </el-col>
                    <el-col :span="12">
                        <el-card>
                            <template #header>考勤统计（近30天）</template>
                            <div v-if="childAttendance" v-loading="attendanceLoading">
                                <p>正常: {{ childAttendance.presentDays || 0 }} 天</p>
                                <p>迟到: {{ childAttendance.lateDays || 0 }} 天</p>
                                <p>缺勤: {{ childAttendance.absentDays || 0 }} 天</p>
                                <p>请假: {{ childAttendance.leaveDays || 0 }} 天</p>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>

                <!-- 家长额外显示孩子列表 -->
                <el-row style="margin-top: 20px">
                    <el-col :span="24">
                        <el-card>
                            <template #header>我的孩子</template>
                            <el-table :data="statistics.children || []" border>
                                <el-table-column prop="name" label="姓名" />
                                <el-table-column prop="className" label="班级" />
                            </el-table>
                        </el-card>
                    </el-col>
                </el-row>
            </template>
        </template>

        <!-- 快捷入口（所有角色可见） -->
        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="24">
                <el-card>
                    <template #header>快捷入口</template>
                    <el-row :gutter="20">
                        <el-col :xs="12" :sm="8" :md="4" v-for="item in allowedQuickLinks" :key="item.path">
                            <div class="quick-link" @click="goTo(item.path)">
                                <el-icon :size="32"><component :is="item.icon" /></el-icon>
                                <span>{{ item.name }}</span>
                            </div>
                        </el-col>
                    </el-row>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getStatistics } from '@/api/dashboard'
import { getScoreList } from '@/api/score'
import { getAttendanceList } from '@/api/attendance'

const router = useRouter()
const userStore = useUserStore()
const role = userStore.role
const statistics = ref({})

// 孩子相关
const selectedChildId = ref(null)
const childScores = ref([])
const childAttendance = ref(null)
const scoreLoading = ref(false)
const attendanceLoading = ref(false)

// 快捷入口
const allowedQuickLinks = computed(() => {
    const role = userStore.role
    if (role === 'ADMIN' || role === 'TEACHER') {
        return [
            { name: '学生管理', path: '/student', icon: 'User' },
            { name: '成绩管理', path: '/score', icon: 'Document' },
            { name: '考勤管理', path: '/attendance', icon: 'Calendar' },
            { name: '通知公告', path: '/notice', icon: 'Bell' },
            { name: '消息中心', path: '/chat', icon: 'ChatDotRound' }
        ]
    } else {
        return [
            { name: '我的成绩', path: '/my-score', icon: 'Document' },
            { name: '我的考勤', path: '/my-attendance', icon: 'Calendar' },
            { name: '通知公告', path: '/notice', icon: 'Bell' },
            { name: '消息中心', path: '/chat', icon: 'ChatDotRound' }
        ]
    }
})



const goTo = (path) => router.push(path)

// 加载孩子的成绩
const loadChildScores = async (studentId) => {
    if (!studentId) {
        childScores.value = []
        return
    }
    scoreLoading.value = true
    try {
        const res = await getScoreList({ studentId, pageNum: 1, pageSize: 5 })
        if (res.code === 200) {
            childScores.value = res.data.rows || []
        }
    } finally {
        scoreLoading.value = false
    }
}

// 加载孩子的考勤（近30天统计）
const loadChildAttendance = async (studentId) => {
    if (!studentId) {
        childAttendance.value = null
        return
    }
    attendanceLoading.value = true
    try {
        // 注意：考勤列表接口需要支持 studentId 参数，并返回统计？或者调用统计接口。
        // 简单方案：用现有 /attendance/list 获取该学生所有考勤，前端计算统计，或后端提供统计接口。
        // 这里假设后端 AttendanceMapper 已有 getStatByStudentId 方法，但前端需要调用对应接口。
        // 如果没有，可先调用 /attendance/list 然后前端计算（效率较低）。
        // 鉴于您后端已有 DashboardStatisticsVO.AttendanceStat，可新增接口。为了快速实现，先调用 /attendance/list。
        const res = await getAttendanceList({ studentId, pageNum: 1, pageSize: 100 })
        if (res.code === 200) {
            const records = res.data.rows || []
            const now = new Date()
            const oneMonthAgo = new Date()
            oneMonthAgo.setMonth(now.getMonth() - 1)
            const filtered = records.filter(r => new Date(r.attendanceDate) >= oneMonthAgo)
            const stats = {
                presentDays: filtered.filter(r => r.status === 'PRESENT').length,
                lateDays: filtered.filter(r => r.status === 'LATE').length,
                absentDays: filtered.filter(r => r.status === 'ABSENT').length,
                leaveDays: filtered.filter(r => r.status === 'LEAVE').length
            }
            childAttendance.value = stats
        }
    } catch (error) {
        console.error('加载考勤失败', error)
    } finally {
        attendanceLoading.value = false
    }
}

// 孩子切换事件
const handleChildChange = async (val) => {
    if (!val) {
        childScores.value = []
        childAttendance.value = null
        return
    }
    await Promise.all([
        loadChildScores(val),
        loadChildAttendance(val)
    ])
}

// 初始化
onMounted(async () => {
    const res = await getStatistics()
    if (res.code === 200) {
        statistics.value = res.data
        // 如果是家长，且有孩子，默认选中第一个孩子并加载数据
        if (role === 'PARENT' && statistics.value.children && statistics.value.children.length > 0) {
            selectedChildId.value = statistics.value.children[0].id
            await handleChildChange(selectedChildId.value)
        }
    }
})
</script>

<style scoped>
.stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 12px;
    transition: all 0.3s;
}
.stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
}
.stat-icon .el-icon {
    font-size: 32px;
    color: white;
}
.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.green { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
.orange { background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%); }
.purple { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }
.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #2c3e50;
}
.stat-label {
    font-size: 14px;
    color: #606266;
    margin-top: 8px;
}
.quick-link {
    text-align: center;
    cursor: pointer;
    padding: 16px;
    border-radius: 12px;
    transition: all 0.3s;
}
.quick-link:hover {
    background: #f5f7fa;
    transform: translateY(-2px);
}
.quick-link .el-icon {
    color: var(--el-color-primary);
    margin-bottom: 8px;
}
.quick-link span {
    display: block;
    font-size: 14px;
}
</style>