import request from '@/utils/request'

export function getSessions() {
    return request({url: '/chat/sessions', method: 'get'})
}

export function getMessages(sessionId, page = 1, size = 20) {
    return request({url: '/chat/messages', method: 'get', params: {sessionId, page, size}})
}

export function createSingleSession(targetUserId) {
    return request({url: '/chat/session/single', method: 'post', params: {targetUserId}})
}

export function markAsRead(sessionId) {
    return request({url: `/chat/read/${sessionId}`, method: 'put'})
}

export function createGroupSession(groupName, memberIds) {
    return request({
        url: '/chat/group',
        method: 'post',
        params: {
            groupName: groupName,
            memberIds: memberIds.join(',')
        } // 将数组转为逗号分隔字符串
    })
}

// 搜索群聊
export function searchGroups(keyword) {
    return request({ url: '/chat/search/group', method: 'get', params: { keyword } })
}

// 加入群聊
export function joinGroupSession(sessionId) {
    return request({ url: `/chat/group/${sessionId}/join`, method: 'post' })
}

export function updateGroupName(sessionId, groupName) {
    return request({
        url: `/chat/group/${sessionId}/name`,
        method: 'put',
        params: { groupName }
    })
}

// 删除单聊会话
export function deleteSingleSession(sessionId) {
    return request({
        url: `/chat/session/single/${sessionId}`,
        method: 'delete'
    })
}

// 退出群聊
export function quitGroupSession(sessionId) {
    return request({
        url: `/chat/group/${sessionId}/quit`,
        method: 'delete'
    })
}