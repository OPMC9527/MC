import request from '@/utils/request'

/**
 * 获取班级列表（用于下拉选择）
 * @returns {Promise} 返回班级列表，格式：[{ id, className, grade, headTeacherId }]
 */
// 获取班级列表
export function getClassList(params) {
    return request({
        url: '/class/list',
        method: 'get',
        params
    })
}

// 添加班级
export function addClass(data) {
    return request({
        url: '/class',
        method: 'post',
        data
    })
}

// 更新班级
export function updateClass(data) {
    return request({
        url: '/class',
        method: 'put',
        data
    })
}

// 删除班级
export function deleteClass(id) {
    return request({
        url: `/class/${id}`,
        method: 'delete'
    })
}