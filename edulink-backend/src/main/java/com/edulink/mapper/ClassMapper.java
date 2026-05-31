package com.edulink.mapper;

import com.edulink.entity.Class;
import com.edulink.vo.ClassVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 班级数据访问接口
 * 提供对班级表的增删改查操作，支持关联查询班主任姓名
 */
@Mapper
public interface ClassMapper {

    /**
     * 查询所有班级
     * 按 ID 降序排列
     *
     * @return 所有班级实体列表
     */
    @Select("SELECT * FROM class ORDER BY id DESC")
    List<Class> selectAll();

    /**
     * 根据班级ID查询班级
     *
     * @param id 班级ID
     * @return 班级实体，不存在则返回 null
     */
    @Select("SELECT * FROM class WHERE id = #{id}")
    Class selectById(Long id);

    /**
     * 插入新班级
     * 创建时间自动设为当前时间，主键回填到传入的 clazz 对象的 id 属性
     *
     * @param clazz 班级实体（不含 id，id 由数据库生成）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO class(class_name, grade, head_teacher_id, description, create_time) " +
            "VALUES(#{className}, #{grade}, #{headTeacherId}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Class clazz);

    /**
     * 更新班级信息
     * 注意：不会更新 create_time 字段
     *
     * @param clazz 班级实体（必须包含 id）
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的班级）
     */
    @Update("UPDATE class SET class_name=#{className}, grade=#{grade}, " +
            "head_teacher_id=#{headTeacherId}, description=#{description} WHERE id=#{id}")
    int update(Class clazz);

    /**
     * 根据班级ID删除班级
     *
     * @param id 班级ID
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的班级）
     */
    @Delete("DELETE FROM class WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 动态条件查询班级列表（支持班级名称模糊匹配、年级精确筛选）
     * 关联 sys_user 表获取班主任姓名，按班级 ID 降序排列
     *
     * @param className 班级名称（可选，模糊匹配）
     * @param grade     年级（可选，如“高一”）
     * @return 班级视图对象列表，包含班级信息及班主任姓名
     */
    @Select("<script>" +
            "SELECT c.*, u.real_name as headTeacherName FROM class c " +
            "LEFT JOIN sys_user u ON c.head_teacher_id = u.id " +
            "WHERE 1=1 " +
            "<if test='className != null and className != \"\"'> AND c.class_name LIKE CONCAT('%', #{className}, '%')</if>" +
            "<if test='grade != null and grade != \"\"'> AND c.grade = #{grade}</if>" +
            " ORDER BY c.id DESC" +
            "</script>")
    List<ClassVO> selectList(@Param("className") String className, @Param("grade") String grade);

    /**
     * 根据班主任ID查询其负责的所有班级ID
     * 用于判断某教师是否已是班主任，或查询其管理的班级列表
     *
     * @param teacherId 教师用户ID
     * @return 班级ID列表，若无则返回空列表
     */
    @Select("SELECT id FROM class WHERE head_teacher_id = #{teacherId}")
    List<Long> findClassIdsByHeadTeacherId(Long teacherId);

    @Select("SELECT c.head_teacher_id FROM class c " +
            "INNER JOIN student s ON s.class_id = c.id " +
            "WHERE s.id = #{studentId}")
    Long findHeadTeacherIdByStudentId(Long studentId);
}