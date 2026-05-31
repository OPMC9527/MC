import axios from 'axios'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/store/user'
import {ElLoading} from 'element-plus'


let loadingInstance = null
let requestCount = 0

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {
    if (requestCount === 0) {
        loadingInstance = ElLoading.service({fullscreen: true, text: '加载中...'})
    }
    requestCount++
    return config
})

request.interceptors.request.use(config => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
})

request.interceptors.response.use(response => {
    requestCount--
    if (requestCount === 0 && loadingInstance) {
        loadingInstance.close()
        loadingInstance = null
    }
    return response
}, error => {
    requestCount = 0
    if (loadingInstance) {
        loadingInstance.close()
        loadingInstance = null
    }
    return Promise.reject(error)
})

request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            if (res.code === 401) {
                const userStore = useUserStore()
                userStore.logout()
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message))
        }
        return res
    },
    error => {
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request