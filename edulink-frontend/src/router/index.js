import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from '@/store/user'

const routes = [
    {path: '/login', name: 'Login', component: () => import('@/views/Login.vue')},
    {
        path: '/',
        component: () => import('@/components/Layout.vue'),
        redirect: '/login',
        children: [
            {path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue')},
            {path: 'student', name: 'Student', component: () => import('@/views/student/StudentList.vue')},
            {path: 'score', name: 'Score', component: () => import('@/views/score/ScoreList.vue')},
            {path: 'attendance', name: 'Attendance', component: () => import('@/views/attendance/AttendanceList.vue')},
            {path: 'notice', name: 'Notice', component: () => import('@/views/notice/NoticeList.vue')},
            {path: 'chat', name: 'Chat', component: () => import('@/views/chat/Chat.vue')},
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: {title: '个人中心'}
            },
            {
                path: 'my-score',
                name: 'MyScore',
                component: () => import('@/views/score/ScoreList.vue'),
                meta: { title: '我的成绩', requiresAuth: true }
            },
            {
                path: 'my-attendance',
                name: 'MyAttendance',
                component: () => import('@/views/attendance/AttendanceList.vue'),
                meta: { title: '我的考勤', requiresAuth: true }
            },
            {
                path: 'class',
                name: 'Class',
                component: () => import('@/views/class/ClassList.vue'),
                meta: { title: '班级管理' }
            },
            {
                path: 'user-manage',
                name: 'UserManage',
                component: () => import('@/views/UserManage.vue'),
                meta: { title: '用户管理' }
            }
        ]
    }
]

const router = createRouter({history: createWebHistory(), routes})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')

    // 获取用户角色（如果已登录）
    let role = null
    if (token) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        role = userInfo.role
    }

    // 登录页直接放行
    if (to.path === '/login') {
        next()
        return
    }

    // 未登录则跳转登录页
    if (!token) {
        next('/login')
        return
    }

    // 可选：角色路径限制（防止学生直接输入URL访问教师页面）
    const roleRestrictions = {
        '/student': ['STUDENT', 'PARENT'],
        '/score': ['STUDENT', 'PARENT'],
        '/attendance': ['STUDENT', 'PARENT']
    }
    const restrictedRoles = roleRestrictions[to.path]
    if (restrictedRoles && restrictedRoles.includes(role)) {
        next('/dashboard')  // 无权限则跳转工作台
    } else {
        next()
    }
})

export default router