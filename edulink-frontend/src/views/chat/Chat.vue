<template>
    <div class="chat-container">
        <div class="chat-sidebar">
            <div class="sidebar-header">消息列表</div>
            <div class="session-list">
                <div v-for="session in sessions" :key="session.id"
                     :class="['session-item', { active: currentSession?.id === session.id }]">
                    <div class="session-info" @click="selectSession(session)">
                        <div class="session-name">{{ session.targetUserName || session.sessionName || '群聊' }}</div>
                        <div class="last-message">{{ session.lastMessage }}</div>
                    </div>
                    <div class="session-meta">
                        <span class="time">{{ formatTime(session.lastMessageTime) }}</span>
                        <span v-if="session.unreadCount" class="unread-badge">{{ session.unreadCount }}</span>
                        <!-- 群聊且为群主时显示编辑图标 -->
                        <el-icon v-if="session.sessionType === 'GROUP' && session.isCreator"
                                 class="edit-icon" @click.stop="editGroupName(session)">
                            <Edit />
                        </el-icon>
                        <el-dropdown @command="(cmd) => handleSessionAction(cmd, session)">
                            <el-icon class="more-icon"><MoreFilled /></el-icon>
                            <template #dropdown>
                                <el-dropdown-menu>
                                    <el-dropdown-item :command="session.sessionType === 'SINGLE' ? 'delete' : 'quit'">
                                        {{ session.sessionType === 'SINGLE' ? '删除会话' : '退出群聊' }}
                                    </el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>
            </div>
            <div class="sidebar-footer">
                <el-button @click="showNewChat = true" plain>新聊天</el-button>
                <el-button v-if="userStore.role === 'TEACHER' || userStore.role === 'ADMIN'"
                           @click="showCreateGroup = true" plain type="success">创建群聊</el-button>
            </div>
        </div>
        <div class="chat-main" v-if="currentSession">
            <div class="chat-header">{{ currentSession.targetUserName }}</div>
            <div class="chat-messages" ref="messagesContainer">
                <div v-for="msg in messages" :key="msg.id" :class="['message', msg.isSelf ? 'self' : 'other']">
                    <div class="message-header" v-if="!msg.isSelf">
                        <span class="sender-name">{{ msg.senderName }}</span>
                        <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                    </div>
                    <div class="message-content">{{ msg.content }}</div>
                    <div class="message-time" v-if="msg.isSelf">{{ formatTime(msg.createTime) }}</div>
                </div>
            </div>
            <div class="chat-input">
                <el-input v-model="inputMsg" type="textarea" :rows="3" placeholder="输入消息..."/>
                <el-button type="primary" @click="sendMsg">发送</el-button>
            </div>
        </div>
        <div class="chat-empty" v-else>选择会话开始聊天</div>

        <el-dialog v-model="showNewChat" title="新建聊天" width="400px">
            <el-tabs v-model="searchType">
                <el-tab-pane label="搜索用户" name="user">
                    <el-input v-model="searchKeyword" placeholder="输入用户名或真实姓名" @input="searchUsers" />
                    <div class="user-list">
                        <div v-for="user in searchResults" :key="user.id" class="user-item" @click="startChat(user)">
                            <span>{{ user.realName }}</span>
                            <span class="role">{{ user.role }}</span>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="搜索群聊" name="group">
                    <el-input v-model="groupKeyword" placeholder="输入群名称" @input="searchGroups" />
                    <div class="group-list">
                        <div v-for="group in groupResults" :key="group.id" class="group-item" @click="joinGroup(group)">
                            <span>{{ group.sessionName }}</span>
                            <span class="member-count">成员数: {{ group.memberCount }}</span>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-dialog>

        <!-- 创建群聊对话框 -->
        <el-dialog v-model="showCreateGroup" title="创建群聊" width="500px">
            <el-input v-model="groupName" placeholder="群聊名称" />
            <el-select v-model="selectedMembers" multiple filterable placeholder="选择成员" style="width:100%; margin-top:15px;">
                <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.id" />
            </el-select>
            <template #footer>
                <el-button @click="showCreateGroup = false">取消</el-button>
                <el-button type="primary" @click="createGroup">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { MoreFilled ,Edit } from '@element-plus/icons-vue'
import {
    getSessions, getMessages, createSingleSession, markAsRead,
    createGroupSession, deleteSingleSession, quitGroupSession,
    searchGroups as searchGroupsApi, joinGroupSession
} from '@/api/chat'
import { searchUsers as searchUsersApi, getUserList } from '@/api/user'
import websocket from '@/utils/websocket'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const inputMsg = ref('')
const messagesContainer = ref(null)
const showNewChat = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

// 创建群聊相关
const showCreateGroup = ref(false)
const groupName = ref('')
const selectedMembers = ref([])
const userList = ref([])

// 搜索群聊相关
const searchType = ref('user')
const groupKeyword = ref('')
const groupResults = ref([])

const loadSessions = async () => {
    const res = await getSessions()
    if (res.code === 200) sessions.value = res.data
}

const selectSession = async (session) => {
    currentSession.value = session
    const res = await getMessages(session.id)
    if (res.code === 200) messages.value = res.data
    await markAsRead(session.id)
    session.unreadCount = 0
    scrollToBottom()
}

const sendMsg = () => {
    if (!inputMsg.value.trim()) return
    // 获取当前会话信息
    const session = currentSession.value
    const isGroup = session.sessionType === 'GROUP'
    // 单聊时需要 receiverId，群聊时不需要（后端会广播）
    const receiverId = isGroup ? null : session.targetUserId
    websocket.sendMessage({
        sessionId: session.id,
        receiverId: receiverId,
        content: inputMsg.value,
        messageType: 'TEXT'
    })
    // 乐观更新：自己的消息立即显示
    messages.value.push({
        id: Date.now(),
        content: inputMsg.value,
        createTime: new Date(),
        isSelf: true,
        senderId: userStore.userId,
        senderName: userStore.realName
    })
    inputMsg.value = ''
    scrollToBottom()
}

const onNewMessage = (msg) => {
    // 确保 msg 中包含 senderId 和 isSelf
    msg.isSelf = msg.senderId === userStore.userId
    if (currentSession.value?.id === msg.sessionId) {
        messages.value.push(msg)
        scrollToBottom()
        markAsRead(msg.sessionId)
    } else {
        const session = sessions.value.find(s => s.id === msg.sessionId)
        if (session) {
            session.unreadCount = (session.unreadCount || 0) + 1
            // 更新最后一条消息内容
            session.lastMessage = msg.content
            session.lastMessageTime = msg.createTime
        } else {
            loadSessions()
        }
        ElMessage.info(`${msg.senderName}: ${msg.content}`)
    }
}

const scrollToBottom = async () => {
    await nextTick()
    if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
}

const formatTime = (t) => t ? dayjs(t).format('HH:mm') : ''

// 搜索用户
const searchUsers = async () => {
    const keyword = searchKeyword.value.trim()
    if (!keyword) {
        searchResults.value = []
        return
    }
    try {
        const res = await searchUsersApi(keyword)
        if (res.code === 200) {
            let users = res.data
            // 家长：只显示教师
            if (userStore.role === 'PARENT') {
                users = users.filter(u => u.role === 'TEACHER')
            }
            // 家长或学生：只显示教师
            if (userStore.role === 'PARENT' || userStore.role === 'STUDENT') {
                users = users.filter(u => u.role === 'TEACHER')
            }
            searchResults.value = users
        }
    } catch (error) {
        console.error('搜索用户失败', error)
        ElMessage.error('搜索失败')
    }
}

const startChat = async (user) => {
    if (!user.id) {
        ElMessage.error('用户信息无效')
        return
    }
    const res = await createSingleSession(user.id)
    if (res.code === 200) {
        showNewChat.value = false
        await loadSessions()
        const session = sessions.value.find(s => s.id === res.data)
        if (session) selectSession(session)
    }
}

// 加载用户列表（用于创建群聊时选择成员）
const loadUserList = async () => {
    // 只有教师和管理员需要加载用户列表（用于创建群聊）
    if (userStore.role !== 'TEACHER' && userStore.role !== 'ADMIN') {
        userList.value = []
        return
    }
    try {
        const res = await getUserList({ pageNum: 1, pageSize: 100 })
        if (res.code === 200) {
            let users = []
            if (Array.isArray(res.data)) {
                users = res.data
            } else if (res.data && res.data.rows) {
                users = res.data.rows
            }
            userList.value = users.filter(u => u.id !== userStore.userId)
        }
    } catch (error) {
        console.error('加载用户列表失败', error)
    }
}

// 搜索群聊
const searchGroups = async () => {
    const keyword = groupKeyword.value.trim()
    if (!keyword) {
        groupResults.value = []
        return
    }
    try {
        const res = await searchGroupsApi(keyword)
        if (res.code === 200) {
            groupResults.value = res.data
        }
    } catch (error) {
        console.error('搜索群聊失败', error)
        ElMessage.error('搜索失败')
    }
}

// 加入群聊
const joinGroup = async (group) => {
    const res = await joinGroupSession(group.id)
    if (res.code === 200) {
        ElMessage.success('已加入群聊')
        showNewChat.value = false
        loadSessions()
    }
}

// 创建群聊
const createGroup = async () => {
    if (!groupName.value.trim()) {
        ElMessage.warning('请输入群聊名称')
        return
    }
    if (selectedMembers.value.length === 0) {
        ElMessage.warning('请至少选择一名成员')
        return
    }
    const res = await createGroupSession(groupName.value, selectedMembers.value)
    if (res.code === 200) {
        ElMessage.success('群聊创建成功')
        showCreateGroup.value = false
        groupName.value = ''
        selectedMembers.value = []
        await loadSessions()
    }
}

const editGroupName = (session) => {
    ElMessageBox.prompt('请输入新的群聊名称', '修改群名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: session.sessionName,
        inputPattern: /^.{1,30}$/,
        inputErrorMessage: '群名称长度1-30个字符'
    }).then(async ({ value }) => {
        const res = await updateGroupName(session.id, value)
        if (res.code === 200) {
            ElMessage.success('修改成功')
            // 更新本地会话列表中的名称
            session.sessionName = value
            // 如果当前打开的会话就是这个群，也更新聊天头部名称
            if (currentSession.value?.id === session.id) {
                currentSession.value.sessionName = value
            }
        }
    }).catch(() => {})
}

// 删除单聊会话或退出群聊
const handleSessionAction = async (command, session) => {
    if (command === 'delete') {
        await deleteSingleSession(session.id)
        ElMessage.success('已删除会话')
    } else if (command === 'quit') {
        await quitGroupSession(session.id)
        ElMessage.success('已退出群聊')
    }
    await loadSessions()
    if (currentSession.value?.id === session.id) {
        currentSession.value = null
        messages.value = []
    }
}

onMounted(() => {
    loadSessions()
    websocket.connect(userStore.userId, onNewMessage)
    loadUserList()
})

onUnmounted(() => {
    websocket.disconnect()
})
</script>

<style scoped>
/* 样式部分保持不变，您已有的样式无需修改 */
</style>
<style scoped>
.chat-container {
    display: flex;
    height: calc(100vh - 120px);
    background: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.chat-sidebar {
    width: 300px;
    border-right: 1px solid #e4e7ed;
    display: flex;
    flex-direction: column;
    background: #fafbfc;
}

.sidebar-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    font-weight: bold;
    background: white;
}

.session-list {
    flex: 1;
    overflow-y: auto;
}

.session-meta {
    display: flex;
    align-items: center;
    gap: 8px;
}
.more-icon {
    cursor: pointer;
    color: #909399;
    font-size: 16px;
}
.more-icon:hover {
    color: #409eff;
}

.session-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
    transition: background 0.2s;
}

.session-item:hover {
    background: #ecf5ff;
}

.session-item.active {
    background: #e6f7ff;
    border-left: 3px solid var(--el-color-primary);
}

.session-info {
    flex: 1;
}

.session-name {
    font-weight: 500;
    margin-bottom: 4px;
}

.last-message {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.unread-badge {
    background: #f56c6c;
    color: white;
    border-radius: 10px;
    padding: 2px 6px;
    font-size: 12px;
    margin-left: 8px;
}

.chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: white;
}

.chat-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    background: #f8f9fc;
    font-weight: 500;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #f5f7fa;
}

.message {
    margin-bottom: 16px;
    display: flex;
    flex-direction: column;
}
.message.self {
    align-items: flex-end;
}
.message-header {
    margin-bottom: 4px;
    font-size: 12px;
    color: #909399;
}
.sender-name {
    font-weight: 500;
    color: #409eff;
}
.message.self .message-header {
    display: none;
}
.message-content {
    max-width: 70%;
    padding: 8px 12px;
    border-radius: 12px;
    background: white;
    box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
    word-wrap: break-word;
    display: inline-block;   /* 新增：使气泡宽度自适应内容 */
    text-align: left;        /* 保证文字左对齐 */
}
.message.self .message-content {
    background: #95ec69;
}
.message-time {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
    text-align: right;       /* 时间右对齐 */
}

.chat-input {
    padding: 16px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    gap: 12px;
    align-items: flex-end;
    background: white;
}

.chat-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
}

.edit-icon {
    cursor: pointer;
    color: #909399;
    font-size: 16px;
    margin-left: 8px;
}
.edit-icon:hover {
    color: #409eff;
}
</style>