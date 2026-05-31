import {defineStore} from 'pinia'
import {login as loginApi} from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
    }),
    getters: {
        userId: (state) => state.userInfo.id,
        realName: (state) => state.userInfo.realName,
        role: (state) => state.userInfo.role
    },
    actions: {
        async login(credentials) {
            const res = await loginApi(credentials)
            if (res.code === 200) {
                this.token = res.data.token
                this.userInfo = res.data.userInfo
                localStorage.setItem('token', this.token)
                localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
                return true
            }
            return false
        },
        logout() {
            this.token = ''
            this.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
        }
    }
})