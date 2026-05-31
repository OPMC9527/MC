package com.edulink.mapper;

import com.edulink.dto.StudentQueryDTO;
import com.edulink.entity.Student;
import com.edulink.vo.ChildVO;
import com.edulink.vo.DashboardStatisticsVO;
import com.edulink.vo.StudentVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 学生数据访问接口
 * 提供学生信息的增删改查、关联查询（班级、家长、用户）、统计等功能
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据学生ID查询学生实体（用于更新前获取原数据）
     *
     * @param id 学生ID（即 sys_user.id）
     * @return 学生实体，不存在则返回 null
     */
    @Select("SELECT * FROM student WHERE id = #{id}")
    Student selectById(Long id);

    @Select("SELECT s.id, s.student_number, u.real_name, c.class_name " +
            "FROM student s " +
            "LEFT JOIN sys_user u ON s.id = u.id " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "WHERE s.parent_id = #{parentId}")
    List<ChildVO> findChildrenByParentId(Long parentId);

    /**
     * 根据学号查询学生（用于检查学号是否重复）
     *
     * @param studentNumber 学号
     * @return 学生实体，不存在则返回 null
     */
    @Select("SELECT * FROM student WHERE student_number = #{studentNumber}")
    Student findByStudentNumber(String studentNumber);

    /**
     * 根据学生ID查询学生详情（关联用户、班级、家长信息）
     * 返回包含真实姓名、用户名、手机号、邮箱、班级名称、年级、家长姓名、家长电话等完整信息
     *
     * @param id 学生ID
     * @return 学生视图对象，包含关联的详细信息
     */
    @Select("SELECT s.*, u.real_name, u.username, u.phone, u.email, " +
            "c.class_name, c.grade, " +
            "pu.real_name as parent_name, pu.phone as parent_phone " +
            "FROM student s " +
            "LEFT JOIN sys_user u ON s.id = u.id " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "LEFT JOIN sys_user pu ON s.parent_id = pu.id " +
            "WHERE s.id = #{id}")
    StudentVO selectStudentDetailById(Long id);

    /**
     * 动态条件查询学生列表（支持分页）
     * 支持按班级名称、学生姓名、学号（模糊/精确）、班级ID列表、学生ID列表等条件筛选
     * 结果按学生ID降序排列
     *
     * @param queryDTO 查询条件对象（包含 className、studentName、studentNumber、classIds 等可选参数）
     * @return 学生视图对象列表
     */
    @Select("<script>" +
            "SELECT s.*, u.real_name, u.username, u.phone, u.email, " +
            "c.class_name, c.grade " +
            "FROM student s " +
            "LEFT JOIN sys_user u ON s.id = u.id " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "WHERE 1=1 " +
            "<if test='className != null and className != \"\"'> AND c.class_name LIKE CONCAT('%', #{className}, '%')</if>" +
            "<if test='studentName != null and studentName != \"\"'> AND u.real_name LIKE CONCAT('%', #{studentName}, '%')</if>" +
            "<if test='studentNumber != null and studentNumber != \"\"'>" +
            "  AND s.student_number " +
            "  <choose>" +
            "    <when test='exactMatch == true'> = #{studentNumber} </when>" +
            "    <otherwise> LIKE CONCAT('%', #{studentNumber}, '%') </otherwise>" +
            "  </choose>" +
            "</if>" +
            "<if test=\"classIds != null and classIds.size() > 0\">\n" +
            "    AND s.class_id IN\n" +
            "    <foreach collection=\"classIds\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n" +
            "        #{id}\n" +
            "    </foreach>\n" +
            "</if>" +
            "<if test='studentId != null'> AND s.id = #{studentId}</if>" +
            "<if test='studentIds != null and studentIds.size() > 0'> AND s.id IN " +
            "<foreach collection='studentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            " ORDER BY s.id DESC" +
            "</script>")
    List<StudentVO> selectStudentList(StudentQueryDTO queryDTO);

    /**
     * 插入新学生记录
     * 学生ID与 sys_user.id 保持一致（一对一关系），创建时间自动设为当前时间
     *
     * @param student 学生实体（必须包含 id，其他字段按需填写）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO student(id, student_number, class_id, gender, birth_date, parent_id, create_time) " +
            "VALUES(#{id}, #{studentNumber}, #{classId}, #{gender}, #{birthDate}, #{parentId}, NOW())")
    int insert(Student student);

    /**
     * 更新学生信息
     *
     * @param student 学生实体（必须包含 id）
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的记录）
     */
    @Update("UPDATE student SET student_number=#{studentNumber}, class_id=#{classId}, " +
            "gender=#{gender}, birth_date=#{birthDate}, parent_id=#{parentId} WHERE id=#{id}")
    int update(Student student);

    /**
     * 根据学生ID删除学生记录
     *
     * @param id 学生ID
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的记录）
     */
    @Delete("DELETE FROM student WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 根据用户ID查询学生记录ID（用于判断用户是否为已注册学生）
     *
     * @param userId 用户ID（即学生ID）
     * @return 学生ID，若不存在则返回 null
     */
    @Select("SELECT id FROM student WHERE id = #{userId}")
    Long findIdByUserId(Long userId);

    /**
     * 根据家长ID查询所有关联的学生ID列表
     * 用于家长端展示多个孩子的信息
     *
     * @param parentId 家长用户ID
     * @return 学生ID列表，若无则返回空列表
     */
    @Select("SELECT id FROM student WHERE parent_id = #{parentId}")
    List<Long> findIdsByParentId(Long parentId);

    /**
     * 统计学生总数（全局统计）
     * 用于管理员/教师仪表盘
     *
     * @return 学生总数
     */
    @Select("SELECT COUNT(*) FROM student")
    Long countTotal();

    /**
     * 查询孩子的基本信息（用于家长工作台展示）
     *
     * @param studentId 学生ID
     * @return 孩子信息对象（包含学生ID、姓名、班级名称）
     */
    @Select("SELECT s.id, u.real_name as name, c.class_name as className " +
            "FROM student s " +
            "LEFT JOIN sys_user u ON s.id = u.id " +
            "LEFT JOIN class c ON s.class_id = c.id " +
            "WHERE s.id = #{studentId}")
    DashboardStatisticsVO.ChildInfo getChildInfo(Long studentId);

}