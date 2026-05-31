package com.edulink.mapper;

import com.edulink.entity.ChatMessage;
import com.edulink.entity.ChatSession;
import com.edulink.vo.ChatMessageVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 聊天消息数据访问接口
 * 提供聊天消息的增删改查、未读计数、消息状态更新等功能
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入一条新消息
     * 消息ID由数据库自动生成并回填
     *
     * @param message 聊天消息实体（不含 id，其他字段需填充）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO chat_message(session_id, sender_id, sender_name, sender_role, sender_avatar, message_type, content, is_read, create_time) " +
            "VALUES(#{sessionId}, #{senderId}, #{senderName}, #{senderRole}, #{senderAvatar}, #{messageType}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    /**
     * 分页查询指定会话的历史消息
     * 按创建时间倒序排列（最新的在前）
     *
     * @param sessionId 会话ID
     * @param offset    偏移量（从0开始）
     * @param limit     每页条数
     * @return 消息视图对象列表
     */
    @Select("SELECT id, session_id, sender_id, sender_name, sender_role, sender_avatar, content, message_type, create_time " +
            "FROM chat_message WHERE session_id = #{sessionId} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<ChatMessageVO> selectMessagesBySessionId(@Param("sessionId") Long sessionId,
                                                  @Param("offset") Integer offset,
                                                  @Param("limit") Integer limit);

    /**
     * 将指定会话中所有未读消息标记为已读
     * 排除当前用户自己发送的消息
     *
     * @param sessionId 会话ID
     * @param userId    当前登录用户ID（接收方）
     * @return 更新的消息条数
     */
    @Update("UPDATE chat_message SET is_read = 1 WHERE session_id = #{sessionId} AND sender_id != #{userId} AND is_read = 0")
    int updateReadStatus(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 统计指定会话中当前用户未读的消息数量
     * 排除当前用户自己发送的消息
     *
     * @param sessionId 会话ID
     * @param userId    当前登录用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND sender_id != #{userId} AND is_read = 0")
    int selectUnreadCount(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 根据会话ID删除会话（物理删除）
     * 注意：此方法直接删除 chat_session 表中的记录，通常不单独使用，应与消息级联删除配合
     *
     * @param sessionId 会话ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM chat_session WHERE id = #{sessionId}")
    int deleteById(Long sessionId);

    /**
     * 根据会话ID查询会话信息
     *
     * @param sessionId 会话ID
     * @return 会话实体，不存在则返回 null
     */
    @Select("SELECT * FROM chat_session WHERE id = #{sessionId}")
    ChatSession selectById(Long sessionId);

    /**
     * 删除指定会话下的所有消息（物理删除）
     * 通常用于清空会话或删除会话前的数据清理
     *
     * @param sessionId 会话ID
     * @return 删除的消息条数
     */
    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    int deleteBySessionId(Long sessionId);

    @Delete("DELETE FROM chat_message WHERE sender_id = #{senderId}")
    int deleteBySenderId(Long senderId);
}