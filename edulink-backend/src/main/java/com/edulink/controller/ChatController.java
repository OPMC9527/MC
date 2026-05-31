package com.edulink.controller;

import com.edulink.entity.ChatSession;
import com.edulink.mapper.ChatSessionMapper;
import com.edulink.service.ChatService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.ChatMessageVO;
import com.edulink.vo.ChatSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 * 提供会话管理、消息查询、群组操作等 WebSocket 相关的 HTTP 辅助接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    /**
     * 获取当前用户的所有会话列表（包括单聊和群聊）
     *
     * @return 会话列表结果对象
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> getSessions() {
        Long userId = UserContext.getUserId();
        return chatService.getUserSessions(userId);
    }

    /**
     * 分页获取指定会话的历史消息
     *
     * @param sessionId 会话ID
     * @param page      页码（默认1）
     * @param size      每页条数（默认20）
     * @return 消息列表结果对象
     */
    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> getMessages(@RequestParam Long sessionId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return chatService.getMessages(sessionId, userId, page, size);
    }

    /**
     * 创建单聊会话（如果已存在则返回现有会话ID）
     *
     * @param targetUserId 目标用户ID
     * @return 会话ID结果对象
     */
    @PostMapping("/session/single")
    public Result<Long> createSingleSession(@RequestParam Long targetUserId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        if (targetUserId == null) {
            return Result.error("目标用户ID不能为空");
        }
        return chatService.createSingleSession(currentUserId, targetUserId);
    }

    /**
     * 将指定会话中的消息标记为已读
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @PutMapping("/read/{sessionId}")
    public Result<?> markAsRead(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        return chatService.markAsRead(sessionId, userId);
    }

    /**
     * 创建群聊会话
     *
     * @param groupName 群聊名称
     * @param memberIds 初始成员ID列表（包含创建者）
     * @return 新群聊会话ID结果对象
     */
    @PostMapping("/group")
    public Result<Long> createGroup(@RequestParam String groupName,
                                    @RequestParam List<Long> memberIds) {
        Long creatorId = UserContext.getUserId();
        return chatService.createGroupSession(groupName, memberIds, creatorId);
    }

    /**
     * 向群聊中添加成员
     *
     * @param sessionId 群聊会话ID
     * @param userId    待添加的用户ID
     * @return 操作结果
     */
    @PostMapping("/group/{sessionId}/member")
    public Result<?> addMember(@PathVariable Long sessionId, @RequestParam Long userId) {
        return chatService.addGroupMember(sessionId, userId);
    }

    /**
     * 从群聊中移除成员
     *
     * @param sessionId 群聊会话ID
     * @param userId    待移除的用户ID
     * @return 操作结果
     */
    @DeleteMapping("/group/{sessionId}/member/{userId}")
    public Result<?> removeMember(@PathVariable Long sessionId, @PathVariable Long userId) {
        return chatService.removeGroupMember(sessionId, userId);
    }

    /**
     * 获取群聊的所有成员ID列表
     *
     * @param sessionId 群聊会话ID
     * @return 成员ID列表结果对象
     */
    @GetMapping("/session/{sessionId}/members")
    public Result<List<Long>> getMembers(@PathVariable Long sessionId) {
        return chatService.getSessionMembers(sessionId);
    }

    /**
     * 删除单聊会话（软删除，仅对当前用户隐藏）
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/session/single/{sessionId}")
    public Result<?> deleteSingleSession(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        return chatService.deleteSingleSession(sessionId, userId);
    }

    /**
     * 退出群聊（从成员表中移除当前用户）
     *
     * @param sessionId 群聊会话ID
     * @return 操作结果
     */
    @DeleteMapping("/group/{sessionId}/quit")
    public Result<?> quitGroup(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        return chatService.quitGroupSession(sessionId, userId);
    }

    /**
     * 修改群聊名称（仅群主或管理员可操作）
     *
     * @param sessionId 群聊会话ID
     * @param groupName 新群聊名称
     * @return 操作结果
     */
    @PutMapping("/group/{sessionId}/name")
    public Result<?> updateGroupName(@PathVariable Long sessionId, @RequestParam String groupName) {
        Long userId = UserContext.getUserId();
        return chatService.updateGroupName(sessionId, groupName, userId);
    }

    /**
     * 根据关键词搜索群聊（模糊匹配群名称，且返回当前用户已加入的群）
     *
     * @param keyword 搜索关键词
     * @return 匹配的群聊会话列表结果对象
     */
    @GetMapping("/search/group")
    public Result<List<ChatSessionVO>> searchGroups(@RequestParam String keyword) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        List<ChatSessionVO> groups = chatSessionMapper.searchGroups(keyword, currentUserId);
        return Result.success(groups);
    }

    /**
     * 用户主动加入一个群聊（无需邀请）
     * 前提：群聊存在且为群聊类型，当前用户尚未加入
     *
     * @param sessionId 群聊会话ID
     * @return 操作结果
     */
    @PostMapping("/group/{sessionId}/join")
    public Result<?> joinGroup(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        // 检查会话是否存在且为群聊
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || !"GROUP".equals(session.getSessionType())) {
            return Result.error("群聊不存在");
        }
        // 检查是否已是成员
        int count = chatSessionMapper.isMember(sessionId, userId);
        if (count > 0) {
            return Result.error("已经是群成员");
        }
        chatSessionMapper.addMember(sessionId, userId);
        return Result.success("加入成功");
    }
}