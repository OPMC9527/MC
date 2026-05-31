<template>
    <el-container class="layout-container">
        <el-aside width="220px" class="aside">
            <div class="logo">EduLink</div>
            <el-menu router :default-active="$route.path" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
                <!-- 工作台（所有角色可见） -->
                <el-menu-item index="/dashboard">
                    <el-icon><Odometer /></el-icon>
                    <span>工作台</span>
                </el-menu-item>
                <!-- 用户管理（仅管理员） -->
                <el-menu-item v-if="role === 'ADMIN'" index="/user-manage">
                    <el-icon><User /></el-icon>
                    <span>用户管理</span>
                </el-menu-item>

                <!-- 学生管理：教师可见 -->
                <el-menu-item v-if="role === 'TEACHER'" index="/student">
                    <el-icon><User /></el-icon>
                    <span>学生管理</span>
                </el-menu-item>

                <!-- 班级管理（仅管理员） -->
                <el-menu-item v-if="role === 'ADMIN'" index="/class">
                    <el-icon><School /></el-icon>
                    <span>班级管理</span>
                </el-menu-item>

                <!-- 成绩管理：仅管理员和教师可见（教师可增删改） -->
                <el-menu-item v-if="role === 'ADMIN' || role === 'TEACHER'" index="/score">
                    <el-icon><Document /></el-icon>
                    <span>成绩管理</span>
                </el-menu-item>

                <!-- 考勤管理：仅管理员和教师可见 -->
                <el-menu-item v-if="role === 'ADMIN' || role === 'TEACHER'" index="/attendance">
                    <el-icon><Calendar /></el-icon>
                    <span>考勤管理</span>
                </el-menu-item>

                <!-- 学生/家长专用：我的成绩（只读） -->
                <el-menu-item v-if="role === 'STUDENT' || role === 'PARENT'" index="/my-score">
                    <el-icon><Document /></el-icon>
                    <span>我的成绩</span>
                </el-menu-item>

                <!-- 学生/家长专用：我的考勤（只读） -->
                <el-menu-item v-if="role === 'STUDENT' || role === 'PARENT'" index="/my-attendance">
                    <el-icon><Calendar /></el-icon>
                    <span>我的考勤</span>
                </el-menu-item>

                <!-- 通知公告（所有角色可见） -->
                <el-menu-item index="/notice">
                    <el-icon><Bell /></el-icon>
                    <span>通知公告</span>
                </el-menu-item>

                <!-- 消息中心（所有角色可见） -->
                <el-menu-item index="/chat">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>消息中心</span>
                </el-menu-item>
            </el-menu>
        </el-aside>

        <el-container>
            <el-header class="header">
                <div class="user-info">
                    <el-dropdown @command="handleCommand">
                        <span>{{ userStore.realName }} <el-icon><ArrowDown /></el-icon></span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>
            <el-main>
                <router-view />
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'

const router = useRouter()
const userStore = useUserStore()

// 获取当前用户角色
const role = computed(() => userStore.role)

const handleCommand = (cmd) => {
    if (cmd === 'profile') {
        router.push('/profile')
    } else if (cmd === 'logout') {
        userStore.logout()
        ElMessage.success('已退出')
        router.push('/login')
    }
}
</script>

<style scoped>
.layout-container {
    height: 100vh;
}

.aside {
    background-color: #304156;
}

.logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    color: white;
    font-size: 20px;
    font-weight: bold;
    border-bottom: 1px solid #263445;
}

.header {
    background: white;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    border-bottom: 1px solid #e4e7ed;
}

.user-info {
    margin-right: 20px;
    cursor: pointer;
}
</style>