import request from '@/utils/request'

export function getScoreList(params) {
    return request({url: '/score/list', method: 'get', params})
}

export function addScore(data) {
    return request({url: '/score', method: 'post', data})
}

export function updateScore(data) {
    return request({url: '/score', method: 'put', data})
}

export function deleteScore(id) {
    return request({url: `/score/${id}`, method: 'delete'})
}