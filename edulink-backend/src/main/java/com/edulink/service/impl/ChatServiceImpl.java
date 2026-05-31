package com.edulink.service.impl;

import com.edulink.dto.ChatMessageDTO;
import com.edulink.entity.ChatMessage;
import com.edulink.entity.ChatSession;
import com.edulink.entity.User;
import com.edulink.mapper.ChatMessageMapper;
import com.edulink.mapper.ChatSessionMapper;
import com.edulink.mapper.UserMapper;
import com.edulink.service.ChatService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.ChatMessageVO;
import com.edulink.vo.ChatSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 * 提供会话管理（单聊/群聊）、消息发送与接收、未读标记、成员管理等业务功能
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 获取当前用户的所有会话列表（包括单聊和群聊）
     * 对于单聊会话，额外填充对方的用户信息（ID、姓名、角色、头像）
     *
     * @param userId 当前登录用户ID
     * @return 包含会话列表的结果对象
     */
    @Override
    public Result<List<ChatSessionVO>> getUserSessions(Long userId) {
        List<ChatSessionVO> sessions = chatSessionMapper.selectSessionsByUserId(userId);
        String role = UserContext.getRole();  // 获取当前用户角色
        // 为目标用户填充信息（单聊场景）
        for (ChatSessionVO session : sessions) {
            if ("SINGLE".equals(session.getSessionType())) {
                // 获取会话中另一个成员ID
                Long targetUserId = chatSessionMapper.getOtherMemberId(session.getId(), userId);
                if (targetUserId != null) {
                    User targetUser = userMapper.findById(targetUserId);
                    if (targetUser != null) {
                        session.setTargetUserId(targetUserId);
                        session.setTargetUserName(targetUser.getRealName());
                        session.setTargetUserRole(targetUser.getRole());
                        session.setTargetUserAvatar(targetUser.getAvatar());
                    }
                }
            }
        }
        // 学生：保留与教师的单聊 + 所有群聊
        if ("STUDENT".equals(role)) {
            sessions = sessions.stream()
                    .filter(s -> ("SINGLE".equals(s.getSessionType()) && "TEACHER".equals(s.getTargetUserRole()))
                            || "GROUP".equals(s.getSessionType()))
                    .collect(Collectors.toList());
        }

        // 学生和家长：只保留对方角色为教师的单聊会话
        if ("STUDENT".equals(role) || "PARENT".equals(role)) {
            sessions = sessions.stream()
                    .filter(s -> "SINGLE".equals(s.getSessionType()) && "TEACHER".equals(s.getTargetUserRole()))
                    .collect(Collectors.toList());
        }
        return Result.success(sessions);
    }

    /**
     * 分页获取指定会话的历史消息
     * 获取后自动将该会话中当前用户的所有未读消息标记为已读，并为每条消息设置 isSelf 标志
     *
     * @param sessionId 会话ID
     * @param userId    当前用户ID
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @return 消息列表结果对象
     */
    @Override
    public Result<List<ChatMessageVO>> getMessages(Long sessionId, Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<ChatMessageVO> messages = chatMessageMapper.selectMessagesBySessionId(sessionId, offset, size);
        // 标记该会话下的消息为已读
        markAsRead(sessionId, userId);
        // 设置 isSelf 字段，供前端区分自己发送的消息
        for (ChatMessageVO msg : messages) {
            msg.setIsSelf(msg.getSenderId().equals(userId));
        }
        return Result.success(messages);
    }

    /**
     * 发送消息（保存到数据库，并通过WebSocket实时推送给接收者）
     * 同时增加接收者的会话未读计数
     *
     * @param dto      消息内容（会话ID、接收者ID、消息类型、内容）
     * @param senderId 发送者用户ID（从JWT解析）
     * @return 保存后的消息视图对象
     */
    @Override
    @Transactional
    public Result<ChatMessageVO> sendMessage(ChatMessageDTO dto, Long senderId) {
        // 获取发送者信息
        User sender = userMapper.findById(senderId);
        if (sender == null) {
            return Result.error("发送者不存在");
        }
        // 保存消息
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(dto, message);
        message.setSenderId(senderId);
        message.setSenderName(sender.getRealName());
        message.setSenderRole(sender.getRole());
        message.setSenderAvatar(sender.getAvatar());
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "TEXT");
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);

        // 为接收者增加会话的未读计数（用于会话列表展示未读数）
        chatSessionMapper.incrementUnreadCount(dto.getSessionId(), dto.getReceiverId());

        // 构建返回VO
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setIsSelf(true);

        // 通过WebSocket实时推送给接收者（发送到其个人队列 /user/{receiverId}/queue/messages）
        messagingTemplate.convertAndSendToUser(
                String.valueOf(dto.getReceiverId()),
                "/queue/messages",
                vo
        );
        return Result.success(vo);
    }

    /**
     * 创建单聊会话（如果两人已存在单聊会话，则直接返回已有会话ID）
     *
     * @param userId1 用户A
     * @param userId2 用户B
     * @return 会话ID结果对象
     */
    @Override
    @Transactional
    public Result<Long> createSingleSession(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return Result.error("用户ID不能为空");
        }
        // 检查是否已存在单聊会话（双向查找）
        ChatSession exist = chatSessionMapper.selectSingleSession(userId1, userId2);
        if (exist != null) {
            return Result.success(exist.getId());
        }
        // 创建新会话
        ChatSession session = new ChatSession();
        session.setSessionType("SINGLE");
        chatSessionMapper.insert(session);
        // 添加两个成员
        chatSessionMapper.addMember(session.getId(), userId1);
        chatSessionMapper.addMember(session.getId(), userId2);
        return Result.success(session.getId());
    }

    /**
     * 将指定会话中所有未读消息标记为已读
     * 并清空该会话在 chat_member 表中的未读计数
     *
     * @param sessionId 会话ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> markAsRead(Long sessionId, Long userId) {
        // 更新消息表中的 is_read 字段
        chatMessageMapper.updateReadStatus(sessionId, userId);
        // 清空该会话对于该用户的未读计数（chat_member.unread_count）
        chatSessionMapper.clearUnreadCount(sessionId, userId);
        return Result.success("已读", null);
    }

    /**
     * 创建群聊会话
     * 确保创建者自动成为成员，如果 memberIds 中未包含创建者则自动添加
     *
     * @param groupName 群聊名称
     * @param memberIds 初始成员ID列表（包含或不包含创建者均可）
     * @param creatorId 创建者用户ID
     * @return 新群聊会话ID结果对象
     */
    @Override
    @Transactional
    public Result<Long> createGroupSession(String groupName, List<Long> memberIds, Long creatorId) {
        // 1. 创建会话记录
        ChatSession session = new ChatSession();
        session.setSessionType("GROUP");
        session.setSessionName(groupName);
        session.setCreatorId(creatorId);
        chatSessionMapper.insert(session);
        Long sessionId = session.getId();

        // 2. 添加所有成员（包括创建者，如果未包含则自动加入）
        if (!memberIds.contains(creatorId)) {
            memberIds.add(creatorId);
        }
        for (Long userId : memberIds) {
            chatSessionMapper.addMember(sessionId, userId);
        }
        return Result.success(sessionId);
    }

    /**
     * 向群聊中添加新成员
     *
     * @param sessionId 群聊会话ID
     * @param userId    待添加的用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> addGroupMember(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        if (!"GROUP".equals(session.getSessionType())) {
            return Result.error("只能向群聊添加成员");
        }
        chatSessionMapper.addMember(sessionId, userId);
        return Result.success("添加成功");
    }

    /**
     * 从群聊中移除指定成员（仅群主或管理员可操作，此处未做额外权限校验，可后续扩展）
     *
     * @param sessionId 群聊会话ID
     * @param userId    待移除的用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> removeGroupMember(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        if (!"GROUP".equals(session.getSessionType())) {
            return Result.error("只能从群聊移除成员");
        }
        chatSessionMapper.removeMember(sessionId, userId);
        return Result.success("移除成功");
    }

    /**
     * 获取指定会话的所有成员ID列表
     *
     * @param sessionId 会话ID（单聊或群聊）
     * @return 成员ID列表结果对象
     */
    @Override
    public Result<List<Long>> getSessionMembers(Long sessionId) {
        List<Long> memberIds = chatSessionMapper.selectMemberIdsBySessionId(sessionId);
        return Result.success(memberIds);
    }

    /**
     * 修改群聊名称（仅群主可操作）
     *
     * @param sessionId 群聊会话ID
     * @param groupName 新群聊名称
     * @param userId    当前操作用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> updateGroupName(Long sessionId, String groupName, Long userId) {
        // 1. 检查会话是否存在且为群聊
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("群聊不存在");
        }
        if (!"GROUP".equals(session.getSessionType())) {
            return Result.error("只能修改群聊名称");
        }
        // 2. 检查权限：只有群主（创建者）可以修改群名称
        if (!session.getCreatorId().equals(userId)) {
            return Result.error("只有群主可以修改群名称");
        }
        // 3. 更新名称
        session.setSessionName(groupName);
        chatSessionMapper.updateSessionName(session);
        return Result.success("修改成功");
    }

    /**
     * 删除单聊会话（软删除：仅将当前用户从会话成员中移除）
     * 若移除后会话中再无其他成员，则物理删除会话及其所有消息
     *
     * @param sessionId 单聊会话ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> deleteSingleSession(Long sessionId, Long userId) {
        // 1. 检查会话是否存在且为单聊
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        if (!"SINGLE".equals(session.getSessionType())) {
            return Result.error("只能删除单聊会话");
        }
        // 2. 移除当前用户
        chatSessionMapper.removeMember(sessionId, userId);
        // 3. 如果会话中没有其他成员了，则彻底删除会话及其消息
        int memberCount = chatSessionMapper.countMembers(sessionId);
        if (memberCount == 0) {
            chatMessageMapper.deleteBySessionId(sessionId);
            chatSessionMapper.deleteSession(sessionId);
        }
        return Result.success("删除成功");
    }

    /**
     * 退出群聊
     * 当前用户将自己从群聊成员中移除
     * 若退出后群聊再无成员，则物理删除群聊及其所有消息
     *
     * @param sessionId 群聊会话ID
     * @param userId    当前用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> quitGroupSession(Long sessionId, Long userId) {
        // 1. 检查会话是否存在且为群聊
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            return Result.error("会话不存在");
        }
        if (!"GROUP".equals(session.getSessionType())) {
            return Result.error("只能退出群聊");
        }

        // 2. 移除当前用户
        chatSessionMapper.removeMember(sessionId, userId);

        // 3. 检查是否还有其他成员
        int memberCount = chatSessionMapper.countMembers(sessionId);
        if (memberCount == 0) {
            // 4. 先删除该会话下的所有消息（避免外键约束）
            chatMessageMapper.deleteBySessionId(sessionId);
            // 5. 再删除所有成员关联（虽然已经没有成员，但安全起见清理）
            chatSessionMapper.deleteAllMembers(sessionId);
            // 6. 最后删除会话本身
            chatSessionMapper.deleteSession(sessionId);
        }
        return Result.success("已退出群聊");
    }
}