import {Client} from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import {useUserStore} from '@/store/user'

class WebSocketService {
    constructor() {
        this.client = null
        this.connected = false
        this.messageCallbacks = {}
    }

    connect(onMessageReceived) {
        const userStore = useUserStore()
        if (!userStore.token) return
        this.client = new Client({
            webSocketFactory: () => new SockJS(import.meta.env.VITE_WS_URL),
            connectHeaders: {Authorization: `Bearer ${userStore.token}`},
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000
        })
        this.client.onConnect = () => {
            console.log('WebSocket 连接成功')
            this.connected = true
            this.client.subscribe(`/user/${userStore.userId}/queue/messages`, (message) => {
                const received = JSON.parse(message.body)
                if (onMessageReceived) onMessageReceived(received)
                if (this.messageCallbacks[received.sessionId]) {
                    this.messageCallbacks[received.sessionId](received)
                }
            })
        }
        this.client.onStompError = (frame) => console.error('WebSocket 错误', frame)
        this.client.activate()
    }

    sendMessage(message) {
        if (this.client && this.connected) {
            this.client.publish({
                destination: '/app/chat.send',
                body: JSON.stringify(message),
                headers: {Authorization: `Bearer ${useUserStore().token}`}
            })
        }
    }

    onMessage(sessionId, callback) {
        this.messageCallbacks[sessionId] = callback
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate()
            this.connected = false
        }
    }
}

export default new WebSocketService()