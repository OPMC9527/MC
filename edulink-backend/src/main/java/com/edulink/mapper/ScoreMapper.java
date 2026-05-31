package com.edulink.mapper;

import com.edulink.dto.ScoreQueryDTO;
import com.edulink.entity.Score;
import com.edulink.vo.DashboardStatisticsVO;
import com.edulink.vo.ScoreVO;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩数据访问接口
 * 提供成绩记录的增删改查、平均分统计、学生近期成绩查询等功能
 */
@Mapper
public interface ScoreMapper {

    /**
     * 根据成绩ID查询成绩详情
     * 关联学生、班级、教师表，返回包含学生姓名、学号、班级名称、教师姓名等完整信息
     *
     * @param id 成绩记录ID
     * @return 成绩视图对象，包含关联的详细信息，不存在则返回 null
     */
    @Select("SELECT s.*, u.real_name as student_name, stu.student_number, c.class_name, " +
            "t.real_name as teacher_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id " +
            "LEFT JOIN sys_user u ON stu.id = u.id " +
            "LEFT JOIN class c ON stu.class_id = c.id " +
            "LEFT JOIN sys_user t ON s.teacher_id = t.id " +
            "WHERE s.id = #{id}")
    ScoreVO selectScoreDetail(Long id);

    /**
     * 动态条件查询成绩列表（支持分页）
     * 支持按学生ID、学生ID列表、班级ID、学生姓名、班级名称、课程名称、考试类型等条件筛选
     * 结果按考试日期降序排列
     *
     * @param queryDTO 查询条件对象（包含 studentId、classId、courseName、examType 等可选参数）
     * @return 成绩视图对象列表
     */
    @Select("<script>" +
            "SELECT s.*, u.real_name as student_name, stu.student_number, c.class_name, " +
            "t.real_name as teacher_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id " +
            "LEFT JOIN sys_user u ON stu.id = u.id " +
            "LEFT JOIN class c ON stu.class_id = c.id " +
            "LEFT JOIN sys_user t ON s.teacher_id = t.id " +
            "WHERE 1=1 " +
            "<if test='studentId != null'> AND s.student_id = #{studentId}</if>" +
            "<if test='studentIds != null and studentIds.size() > 0'>" +
            " AND s.student_id IN " +
            "<foreach collection='studentIds' item='id' open='(' separator=',' close=')'>" +
            "   #{id}" +
            "</foreach>" +
            "</if>" +
            "<if test='classId != null'> AND stu.class_id = #{classId}</if>" +
            "<if test=\"studentName != null and studentName != ''\">\n" +
            "    AND u.real_name LIKE CONCAT('%', #{studentName}, '%')\n" +
            "</if>\n" +
            "<if test=\"className != null and className != ''\">\n" +
            "    AND c.class_name LIKE CONCAT('%', #{className}, '%')\n" +
            "</if>" +
            "<if test='courseName != null and courseName != \"\"'> AND s.course_name LIKE CONCAT('%', #{courseName}, '%')</if>" +
            "<if test='examType != null and examType != \"\"'> AND s.exam_type = #{examType}</if>" +
            " ORDER BY s.exam_date DESC" +
            "</script>")
    List<ScoreVO> selectScoreList(ScoreQueryDTO queryDTO);

    /**
     * 插入新成绩记录
     * 主键自动生成并回填，创建时间自动设为当前时间
     *
     * @param score 成绩实体（不含 id，其他字段需填充）
     * @return 影响的行数（1 表示成功）
     */
    @Insert("INSERT INTO score(student_id, course_name, score, exam_type, exam_date, teacher_id, create_time) " +
            "VALUES(#{studentId}, #{courseName}, #{score}, #{examType}, #{examDate}, #{teacherId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Score score);

    /**
     * 更新成绩记录
     * 可更新课程名称、分数、考试类型、考试日期、教师ID（注意：不更新创建时间）
     *
     * @param score 成绩实体（必须包含 id）
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的记录）
     */
    @Update("UPDATE score SET course_name=#{courseName}, score=#{score}, exam_type=#{examType}, " +
            "exam_date=#{examDate}, teacher_id=#{teacherId} WHERE id=#{id}")
    int update(Score score);

    /**
     * 根据成绩ID删除成绩记录
     *
     * @param id 成绩记录ID
     * @return 影响的行数（1 表示成功，0 表示未找到对应 id 的记录）
     */
    @Delete("DELETE FROM score WHERE id=#{id}")
    int deleteById(Long id);

    /**
     * 获取所有成绩的平均分（全局统计）
     * 用于管理员/教师仪表盘展示整体学业水平
     *
     * @return 平均分（BigDecimal），若无成绩记录则返回 null
     */
    @Select("SELECT ROUND(IFNULL(AVG(score), 0), 2) FROM score")
    BigDecimal getAverageScore();

    @Delete("DELETE FROM score WHERE student_id = #{studentId}")
    int deleteByStudentId(Long studentId);

    /**
     * 查询指定学生的最近 N 条成绩记录
     * 用于学生个人仪表盘或家长端展示近期成绩趋势
     *
     * @param studentId 学生ID
     * @param limit     返回条数限制
     * @return 近期成绩列表（包含课程名称、分数、考试日期）
     */
    @Select("SELECT course_name, score, DATE_FORMAT(exam_date, '%Y-%m-%d') as examDate " +
            "FROM score WHERE student_id = #{studentId} " +
            "ORDER BY exam_date DESC LIMIT #{limit}")
    List<DashboardStatisticsVO.RecentScore> findRecentScoresByStudentId(@Param("studentId") Long studentId,
                                                                        @Param("limit") int limit);
}