import request from '@/utils/request'

export function getAttendanceList(params) {
    return request({url: '/attendance/list', method: 'get', params})
}

export function checkIn(data) {
    return request({url: '/attendance/checkin', method: 'post', data})
}

export function updateAttendance(data) {
    return request({url: '/attendance', method: 'put', data})
}

export function deleteAttendance(id) {
    return request({url: `/attendance/${id}`, method: 'delete'})
}