package com.edulink.mapper;

import com.edulink.dto.UserQueryDTO;
import com.edulink.entity.User;
import com.edulink.vo.UserSearchVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在则返回 null
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体，不存在则返回 null
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在则返回 null
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 插入新用户
     * 创建时间自动设为当前时间，主键回填到传入的 user 对象的 id 属性
     *
     * @param user 用户实体（不含 id，id 由数据库生成）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO sys_user(username, password, real_name, role, phone, email, status, create_time) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{phone}, #{email}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新用户信息
     *
     * @param user 包含待更新信息的用户实体（必须包含 id）
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的用户）
     */
    @Update("UPDATE sys_user SET username=#{username}, password=#{password}, real_name=#{realName}, role=#{role}, " +
            "phone=#{phone}, email=#{email}, avatar=#{avatar}, status=#{status} WHERE id=#{id}")
    int update(User user);

    /**
     * 根据用户ID删除用户
     *
     * @param id 用户ID
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的用户）
     */
    @Delete("DELETE FROM sys_user WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 统计具有指定角色的用户数量
     *
     * @param role 角色名称（如 "TEACHER"、"STUDENT" 等）
     * @return 该角色的用户总数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE role = #{role}")
    Long countByRole(String role);

    /**
     * 根据关键词搜索用户（用于用户选择/关联场景）
     * 排除当前登录用户，最多返回 20 条，只返回 id、真实姓名、角色、头像字段
     *
     * @param keyword       搜索关键词（模糊匹配真实姓名）
     * @param currentUserId 当前登录用户ID（搜索结果中排除该用户）
     * @return 匹配的用户简要信息列表
     */
    @Select("SELECT id, real_name, role, avatar FROM sys_user WHERE TRIM(real_name) LIKE CONCAT('%', TRIM(#{keyword}), '%') AND id != #{currentUserId} LIMIT 20")
    List<UserSearchVO> searchUsers(@Param("keyword") String keyword, @Param("currentUserId") Long currentUserId);

    /**
     * 获取除当前登录用户外的所有用户（用于管理列表展示）
     * 按创建时间倒序排列，返回 id、用户名、真实姓名、角色、电话、邮箱、状态、创建时间
     *
     * @param currentUserId 当前登录用户ID（排除该用户）
     * @return 用户简要信息列表
     */
    @Select("SELECT id, username, real_name as realName, role, phone, email, status, create_time as createTime " +
            "FROM sys_user WHERE id != #{currentUserId} ORDER BY create_time DESC")
    List<UserSearchVO> listAllExcept(@Param("currentUserId") Long currentUserId);

    /**
     * 根据角色查询用户（用于按角色选择人员）
     * 返回 id、真实姓名、角色、头像，按真实姓名升序排列
     *
     * @param role 角色名称（如 "TEACHER"）
     * @return 该角色的用户简要信息列表
     */
    @Select("SELECT id, real_name as realName, role, avatar FROM sys_user WHERE role = 'TEACHER' ORDER BY real_name")
    List<UserSearchVO> listByRole(String role);

    /**
     * 动态条件查询用户列表（支持用户名、真实姓名、角色筛选）
     * 用于高级搜索或分页查询，按创建时间倒序排列
     *
     * @param queryDTO 查询条件封装对象（包含 username、realName、role 可选字段）
     * @return 符合条件的用户简要信息列表
     */
    @Select("<script>" +
            "SELECT id, username, real_name as realName, role, phone, email, status, create_time as createTime " +
            "FROM sys_user " +
            "WHERE 1=1 " +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%')</if>" +
            "<if test='realName != null and realName != \"\"'> AND real_name LIKE CONCAT('%', #{realName}, '%')</if>" +
            "<if test='role != null and role != \"\"'> AND role = #{role}</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<UserSearchVO> listUsers(UserQueryDTO queryDTO);
}