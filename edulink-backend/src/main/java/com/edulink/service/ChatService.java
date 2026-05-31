package com.edulink.service;

import com.edulink.dto.ChatMessageDTO;
import com.edulink.utils.Result;
import com.edulink.vo.ChatMessageVO;
import com.edulink.vo.ChatSessionVO;

import java.util.List;

public interface ChatService {
    Result<List<ChatSessionVO>> getUserSessions(Long userId);
    Result<List<ChatMessageVO>> getMessages(Long sessionId, Long userId, Integer page, Integer size);
    Result<ChatMessageVO> sendMessage(ChatMessageDTO dto, Long senderId);
    Result<Long> createSingleSession(Long userId1, Long userId2);
    Result<?> markAsRead(Long sessionId, Long userId);

    // 创建群聊会话
    Result<Long> createGroupSession(String groupName, List<Long> memberIds, Long creatorId);

    // 添加群成员
    Result<?> addGroupMember(Long sessionId, Long userId);

    // 移除群成员
    Result<?> removeGroupMember(Long sessionId, Long userId);

    // 获取会话成员ID列表
    Result<List<Long>> getSessionMembers(Long sessionId);

    // 删除单聊会话（从当前用户的会话列表中移除）
    Result<?> deleteSingleSession(Long sessionId, Long userId);

    Result<?> updateGroupName(Long sessionId, String groupName, Long userId);
    // 退出群聊
    Result<?> quitGroupSession(Long sessionId, Long userId);
}
