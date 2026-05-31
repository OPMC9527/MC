import request from '@/utils/request'

export function getStatistics() {
    return request({
        url: '/dashboard/statistics',
        method: 'get'
    })
}

export function getChildStatistics(childId) {
    return request({ url: `/dashboard/child/${childId}`, method: 'get' })
}