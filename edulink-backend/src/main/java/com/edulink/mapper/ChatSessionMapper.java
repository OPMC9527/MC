package com.edulink.mapper;

import com.edulink.entity.ChatSession;
import com.edulink.vo.ChatSessionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    // 查询用户的所有会话（包含最近消息、未读数等）
    @Select("SELECT cs.id, cs.session_type, cs.session_name, cs.group_id, cs.create_time, " +
            "(SELECT content FROM chat_message WHERE session_id = cs.id ORDER BY create_time DESC LIMIT 1) as last_message, " +
            "(SELECT create_time FROM chat_message WHERE session_id = cs.id ORDER BY create_time DESC LIMIT 1) as last_message_time, " +
            "COALESCE(cm.unread_count, 0) as unread_count " +
            "FROM chat_session cs " +
            "LEFT JOIN chat_member cm ON cs.id = cm.session_id AND cm.user_id = #{userId} " +
            "WHERE cs.id IN (SELECT session_id FROM chat_member WHERE user_id = #{userId}) " +
            "ORDER BY last_message_time DESC")
    List<ChatSessionVO> selectSessionsByUserId(Long userId);

    // 查询单聊会话（两个用户之间）
    @Select("SELECT cs.* FROM chat_session cs " +
            "INNER JOIN chat_member cm1 ON cs.id = cm1.session_id AND cm1.user_id = #{userId1} " +
            "INNER JOIN chat_member cm2 ON cs.id = cm2.session_id AND cm2.user_id = #{userId2} " +
            "WHERE cs.session_type = 'SINGLE' LIMIT 1")
    ChatSession selectSingleSession(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 插入会话
    @Insert("INSERT INTO chat_session(session_type, session_name, group_id, creator_id, create_time) " +
            "VALUES(#{sessionType}, #{sessionName}, #{groupId}, #{creatorId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatSession session);

    // 添加会话成员
    @Insert("INSERT INTO chat_member(session_id, user_id, unread_count, join_time) " +
            "VALUES(#{sessionId}, #{userId}, 0, NOW())")
    int addMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    // 获取单聊会话中的另一个成员ID（用于补充 targetUser 信息）
    @Select("SELECT user_id FROM chat_member WHERE session_id = #{sessionId} AND user_id != #{currentUserId} LIMIT 1")
    Long getOtherMemberId(@Param("sessionId") Long sessionId, @Param("currentUserId") Long currentUserId);

    // 移除成员
    @Delete("DELETE FROM chat_member WHERE session_id = #{sessionId} AND user_id = #{userId}")
    int removeMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Delete("DELETE FROM chat_member WHERE session_id = #{sessionId}")
    int deleteAllMembers(Long sessionId);

    // 检查会话中是否还有成员（用于自动清理会话）
    @Select("SELECT COUNT(*) FROM chat_member WHERE session_id = #{sessionId}")
    int countMembers(Long sessionId);

    // 删除会话（无成员时调用）
    @Delete("DELETE FROM chat_session WHERE id = #{sessionId}")
    int deleteSession(Long sessionId);

    // 更新成员的未读计数（在收到新消息时增加）
    @Update("UPDATE chat_member SET unread_count = unread_count + 1 WHERE session_id = #{sessionId} AND user_id = #{userId}")
    int incrementUnreadCount(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    // 清零未读计数（用户打开会话时）
    @Update("UPDATE chat_member SET unread_count = 0 WHERE session_id = #{sessionId} AND user_id = #{userId}")
    int clearUnreadCount(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    // 查询会话的所有成员ID
    @Select("SELECT user_id FROM chat_member WHERE session_id = #{sessionId}")
    List<Long> selectMemberIdsBySessionId(Long sessionId);

    // 根据会话ID查询完整会话实体
    @Select("SELECT * FROM chat_session WHERE id = #{id}")
    ChatSession selectById(Long id);

    @Select("SELECT COUNT(*) FROM chat_member WHERE session_id = #{sessionId} AND user_id = #{userId}")
    int isMember(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 搜索未加入的群聊（按群名称模糊匹配）
     *
     * @param keyword       搜索关键字
     * @param currentUserId 当前用户ID
     * @return 群聊列表（包含成员数）
     */
    @Select("SELECT cs.id, cs.session_name, cs.session_type, cs.group_id, cs.create_time, " +
            "(SELECT COUNT(*) FROM chat_member WHERE session_id = cs.id) as memberCount " +
            "FROM chat_session cs " +
            "WHERE cs.session_type = 'GROUP' " +
            "AND cs.session_name LIKE CONCAT('%', #{keyword}, '%') " +
            "AND cs.id NOT IN (SELECT session_id FROM chat_member WHERE user_id = #{currentUserId})")
    List<ChatSessionVO> searchGroups(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);

    // 更新会话名称（群聊用）
    @Update("UPDATE chat_session SET session_name = #{sessionName} WHERE id = #{id}")
    int updateSessionName(ChatSession session);

    @Delete("DELETE FROM chat_member WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}