<template>
    <div class="page-container">
        <el-card class="profile-card">
            <template #header>
                <div class="card-header">
                    <span class="title">个人中心</span>
                </div>
            </template>
            <el-tabs v-model="activeTab">
                <el-tab-pane label="基本信息" name="info">
                    <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
                        <el-form-item label="用户名">
                            <el-input v-model="profileForm.username" disabled/>
                        </el-form-item>
                        <el-form-item label="真实姓名">
                            <el-input v-model="profileForm.realName" disabled/>
                        </el-form-item>
                        <el-form-item label="角色">
                            <el-input v-model="profileForm.role" disabled/>
                        </el-form-item>
                        <el-form-item label="手机号" prop="phone">
                            <el-input v-model="profileForm.phone" placeholder="请输入手机号"/>
                        </el-form-item>
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="profileForm.email" placeholder="请输入邮箱"/>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="updateProfile" :loading="updateLoading">保存修改
                            </el-button>
                        </el-form-item>
                    </el-form>

                    <!-- 家长专用：我的孩子列表 -->
                    <el-divider v-if="userStore.role === 'PARENT'" content-position="left">我的孩子</el-divider>
                    <div v-if="userStore.role === 'PARENT'">
                        <el-table :data="childrenList" border style="margin-bottom: 15px;">
                            <el-table-column prop="studentNumber" label="学号"/>
                            <el-table-column prop="realName" label="姓名"/>
                            <el-table-column prop="className" label="班级"/>
                            <el-table-column label="操作" width="80">
                                <template #default="{ row }">
                                    <el-button link type="danger" @click="unbindChild(row.id)">解绑</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-form :inline="true">
                            <el-form-item label="绑定新孩子">
                                <el-input v-model="bindStudentNumber" placeholder="输入孩子学号"/>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="bindChild" :loading="bindLoading">绑定</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-tab-pane>

                <el-tab-pane label="修改密码" name="password">
                    <!-- 密码修改表单 -->
                    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
                        <el-form-item label="旧密码" prop="oldPassword">
                            <el-input v-model="pwdForm.oldPassword" type="password" show-password/>
                        </el-form-item>
                        <el-form-item label="新密码" prop="newPassword">
                            <el-input v-model="pwdForm.newPassword" type="password" show-password/>
                        </el-form-item>
                        <el-form-item label="确认新密码" prop="confirmPassword">
                            <el-input v-model="pwdForm.confirmPassword" type="password" show-password/>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="updatePassword" :loading="pwdLoading">修改密码</el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import {reactive, ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
    getProfile,
    updateProfile as updateProfileApi,
    updatePassword as updatePasswordApi,
    bindChild as bindChildApi,
    unbindChild as unbindChildApi,
    getChildren
} from '@/api/user'
import {useUserStore} from '@/store/user'

const userStore = useUserStore()
const activeTab = ref('info')
const updateLoading = ref(false)
const pwdLoading = ref(false)
const bindLoading = ref(false)
const childrenList = ref([])
const bindStudentNumber = ref('')

const profileForm = reactive({
    username: '',
    realName: '',
    role: '',
    phone: '',
    email: ''
})

const profileFormRef = ref()
const profileRules = {
    phone: [{pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur'}],
    email: [{type: 'email', message: '邮箱格式不正确', trigger: 'blur'}]
}

const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})
const pwdFormRef = ref()
const pwdRules = {
    oldPassword: [{required: true, message: '请输入旧密码', trigger: 'blur'}],
    newPassword: [
        {required: true, message: '请输入新密码', trigger: 'blur'},
        {min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur'}
    ],
    confirmPassword: [
        {required: true, message: '请确认新密码', trigger: 'blur'},
        {
            validator: (rule, value, callback) => {
                if (value !== pwdForm.newPassword) callback(new Error('两次输入密码不一致'))
                else callback()
            },
            trigger: 'blur'
        }
    ]
}

// 加载个人信息
const loadProfile = async () => {
    const res = await getProfile()
    if (res.code === 200) {
        Object.assign(profileForm, res.data)
    }
}

// 加载孩子列表
const loadChildren = async () => {
    if (userStore.role !== 'PARENT') return
    const res = await getChildren()
    if (res.code === 200) childrenList.value = res.data
}

// 绑定孩子
const bindChild = async () => {
    if (!bindStudentNumber.value.trim()) {
        ElMessage.warning('请输入孩子学号')
        return
    }
    bindLoading.value = true
    try {
        const res = await bindChildApi(bindStudentNumber.value)
        if (res.code === 200) {
            ElMessage.success('绑定成功')
            bindStudentNumber.value = ''
            loadChildren() // 刷新孩子列表
        }
    } finally {
        bindLoading.value = false
    }
}

// 解绑孩子
const unbindChild = async (studentId) => {
    ElMessageBox.confirm('确认解绑该孩子？', '提示', {type: 'warning'}).then(async () => {
        const res = await unbindChildApi(studentId)
        if (res.code === 200) {
            ElMessage.success('解绑成功')
            loadChildren()
        }
    })
}

// 更新个人信息
const updateProfile = async () => {
    await profileFormRef.value.validate()
    updateLoading.value = true
    try {
        const res = await updateProfileApi({
            phone: profileForm.phone,
            email: profileForm.email
        })
        if (res.code === 200) {
            ElMessage.success('更新成功')
            userStore.userInfo.phone = profileForm.phone
            userStore.userInfo.email = profileForm.email
            localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
        }
    } finally {
        updateLoading.value = false
    }
}

// 修改密码
const updatePassword = async () => {
    try {
        await pwdFormRef.value.validate()
    } catch (err) {
        ElMessage.warning('请正确填写表单')
        return
    }
    pwdLoading.value = true
    try {
        const res = await updatePasswordApi({
            oldPassword: pwdForm.oldPassword,
            newPassword: pwdForm.newPassword
        })
        if (res.code === 200) {
            ElMessage.success('密码修改成功，3秒后跳转到登录页')
            setTimeout(() => {
                userStore.logout()
                window.location.href = '/login'
            }, 3000)
        } else {
            ElMessage.error(res.message || '修改失败')
        }
    } catch (error) {
        ElMessage.error('网络错误或服务器异常')
    } finally {
        pwdLoading.value = false
    }
}

onMounted(() => {
    loadProfile()
    loadChildren()
})
</script>

<style scoped>
.profile-card {
    max-width: 600px;
    margin: 0 auto;
}

.card-header {
    font-size: 18px;
    font-weight: 600;
}

.title {
    border-left: 4px solid var(--el-color-primary);
    padding-left: 12px;
}
</style>