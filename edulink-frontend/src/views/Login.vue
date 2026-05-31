<template>
    <div class="login-container">
        <el-card class="login-card" shadow="hover">
            <div class="login-header">
                <h2>家校协同学生信息管理系统</h2>
                <p>EduLink - 连接家校，共创未来</p>
            </div>
            <el-form :model="form" :rules="rules" ref="formRef" autocomplete="off">
                <el-form-item prop="username">
                    <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" autocomplete="off"/>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" type="password" placeholder="密码" autocomplete="new-password" show-password/>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="large" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const handleLogin = async () => {
    await formRef.value.validate()
    loading.value = true
    try {
        const success = await userStore.login(form)
        if (success) {
            ElMessage.success('登录成功')
            router.push('/dashboard')
        } else {
            ElMessage.error('用户名或密码错误')
        }
    } finally {
        loading.value = false
    }
}

// 每次进入登录页时清空表单
onMounted(() => {
    form.username = ''
    form.password = ''
})
</script>

<style scoped>
.login-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
    width: 450px;
    border-radius: 16px;
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.95);
}

.login-header {
    text-align: center;
    margin-bottom: 30px;
}

.login-header h2 {
    color: #2c3e50;
    margin-bottom: 10px;
}

.login-header p {
    color: #606266;
    font-size: 14px;
}

.demo-tips {
    margin-top: 20px;
    padding: 16px;
    background: #f8f9fc;
    border-radius: 12px;
    font-size: 13px;
    color: #606266;
}

.demo-tips p {
    margin: 6px 0;
}
</style>