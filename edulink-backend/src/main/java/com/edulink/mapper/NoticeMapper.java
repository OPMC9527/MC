package com.edulink.mapper;

import com.edulink.dto.NoticeQueryDTO;
import com.edulink.entity.Notice;
import com.edulink.vo.NoticeVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 通知公告数据访问接口
 * 提供通知的增删改查、浏览量统计、今日通知数量统计等功能
 */
@Mapper
public interface NoticeMapper {

    /**
     * 根据通知ID查询通知详情（包含发布人姓名）
     *
     * @param id 通知ID
     * @return 通知视图对象，包含标题、内容、发布人姓名、紧急程度等，不存在则返回 null
     */
    @Select("SELECT n.*, u.real_name as publisher_name " +
            "FROM notice n LEFT JOIN sys_user u ON n.publisher_id = u.id " +
            "WHERE n.id = #{id}")
    NoticeVO selectById(Long id);

    /**
     * 动态条件查询通知列表（支持分页）
     * 支持按标题模糊查询、按紧急程度筛选，结果按紧急程度降序、创建时间降序排列
     *
     * 查询条件对象（包含 title、isUrgent 等可选参数，以及分页参数）
     * @return 通知视图对象列表
     */
    @Select("<script>" +
            "SELECT n.*, u.real_name as publisher_name " +
            "FROM notice n " +
            "LEFT JOIN sys_user u ON n.publisher_id = u.id " +
            "WHERE 1=1 " +
            "<if test='title != null and title != \"\"'> AND n.title LIKE CONCAT('%', #{title}, '%')</if>" +
            "<if test='isUrgent != null'> AND n.is_urgent = #{isUrgent}</if>" +
            "<if test='role != null and role != \"\"'>" +
            " AND (n.target_roles IS NULL OR n.target_roles = '' OR JSON_CONTAINS(n.target_roles, CONCAT('\"', #{role}, '\"')))" +
            "</if>" +
            " ORDER BY n.is_urgent DESC, n.create_time DESC" +
            "</script>")
    List<NoticeVO> selectList(@Param("title") String title,
                              @Param("isUrgent") Integer isUrgent,
                              @Param("role") String role);

    /**
     * 插入新通知
     * 主键自动生成并回填，初始 view_count 为 0，create_time 为当前时间
     *
     * @param notice 通知实体（不含 id，其他字段需填充）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO notice(title, content, publisher_id, target_roles, is_urgent, view_count, create_time) " +
            "VALUES(#{title}, #{content}, #{publisherId}, #{targetRoles}, #{isUrgent}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notice notice);

    /**
     * 更新通知信息
     * 可更新标题、内容、目标角色、紧急程度（注意：不更新浏览量、创建时间、发布人）
     *
     * @param notice 通知实体（必须包含 id）
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的通知）
     */
    @Update("UPDATE notice SET title=#{title}, content=#{content}, target_roles=#{targetRoles}, " +
            "is_urgent=#{isUrgent} WHERE id=#{id}")
    int update(Notice notice);

    /**
     * 根据通知ID删除通知
     *
     * @param id 通知ID
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的通知）
     */
    @Delete("DELETE FROM notice WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 增加通知的浏览量（每次查看时调用）
     *
     * @param id 通知ID
     * @return 影响的行数（1 表示成功）
     */
    @Update("UPDATE notice SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    /**
     * 统计今日发布的通知数量
     * 用于仪表盘展示当日通知数
     *
     * @return 今日发布的通知总数
     */
    @Select("SELECT COUNT(*) FROM notice WHERE DATE(create_time) = CURDATE()")
    Integer countToday();

    @Delete("DELETE FROM notice WHERE publisher_id = #{publisherId}")
    int deleteByPublisherId(Long publisherId);
}