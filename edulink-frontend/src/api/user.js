import request from '@/utils/request'

// 登录
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 获取个人信息
export function getProfile() {
    return request({
        url: '/user/profile',
        method: 'get'
    })
}

export function searchUsers(keyword) {
    return request({url: '/user/search', method: 'get', params: {keyword}})
}

// 更新个人信息
export function updateProfile(data) {
    return request({ url: '/user/profile', method: 'put', data })
}

// 修改密码
export function updatePassword(data) {
    console.log('updatePassword called, method: put')
    return request({ url: '/user/password', method: 'put', data })
}

// 获取用户列表（管理员用，支持分页和搜索）
export function getUserList(params) {
    return request({ url: '/user/list', method: 'get', params })
}

// 添加用户（管理员）
export function addUser(data) {
    return request({ url: '/user/register', method: 'post', data })
}

// 编辑用户
export function updateUser(data) {
    return request({ url: `/user/${data.id}`, method: 'put', data })
}

// 删除用户
export function deleteUser(id) {
    return request({ url: `/user/${id}`, method: 'delete' })
}

// 重置密码
export function resetPassword(id) {
    return request({ url: `/user/${id}/reset-password`, method: 'put' })
}

// 禁用/启用用户
export function toggleUserStatus(id, status) {
    return request({ url: `/user/${id}/status`, method: 'put', params: { status } })
}

// 获取所有教师（用于班主任下拉选择）
export function getTeacherList() {
    return request({
        url: '/user/teachers',
        method: 'get'
    })
}

// 获取家长的所有孩子
export function getChildren() {
    return request({ url: '/user/parent/children', method: 'get' })
}

// 绑定孩子（通过学号）
export function bindChild(studentNumber) {
    return request({ url: '/user/parent/child', method: 'post', params: { studentNumber } })
}

// 解绑孩子
export function unbindChild(studentId) {
    return request({ url: `/user/parent/child/${studentId}`, method: 'delete' })
}