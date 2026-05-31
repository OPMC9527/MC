package com.edulink.mapper;

import com.edulink.dto.AttendanceQueryDTO;
import com.edulink.entity.Attendance;
import com.edulink.vo.AttendanceVO;
import com.edulink.vo.DashboardStatisticsVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤记录数据访问接口
 * 提供考勤记录的查询、新增、更新、删除以及统计功能
 */
@Mapper
public interface AttendanceMapper {

    /**
     * 根据考勤记录ID查询考勤详情（包含学生姓名、班级等关联信息）
     *
     * @param id 考勤记录ID
     * @return 考勤视图对象，不存在则返回 null
     */
    AttendanceVO selectById(Long id);

    /**
     * 根据查询条件动态查询考勤记录列表
     * 支持按班级、学生姓名、学生ID列表、日期范围、状态等条件筛选
     *
     * @param queryDTO 查询条件封装对象（包含分页、筛选参数）
     * @return 考勤视图对象列表
     */
    List<AttendanceVO> selectList(AttendanceQueryDTO queryDTO);

    /**
     * 根据学生ID和考勤日期查询考勤记录（用于判断是否已签到）
     *
     * @param studentId 学生ID
     * @param date      考勤日期
     * @return 考勤实体，不存在则返回 null
     */
    Attendance selectByStudentAndDate(@Param("studentId") Long studentId, @Param("date") LocalDate date);

    /**
     * 插入新考勤记录
     *
     * @param attendance 考勤实体（不含ID，ID由数据库生成）
     * @return 影响的行数（1表示成功）
     */
    int insert(Attendance attendance);

    /**
     * 更新考勤记录
     *
     * @param attendance 考勤实体（必须包含ID）
     * @return 影响的行数（1表示成功，0表示未找到对应记录）
     */
    int update(Attendance attendance);

    /**
     * 根据考勤记录ID删除记录
     *
     * @param id 考勤记录ID
     * @return 影响的行数（1表示成功，0表示未找到对应记录）
     */
    int deleteById(Long id);

    @Delete("DELETE FROM attendance WHERE student_id = #{studentId}")
    int deleteByStudentId(Long studentId);

    /**
     * 统计指定学生在指定时间段内的考勤汇总数据（如正常次数、迟到次数、缺勤次数、请假次数）
     * 用于仪表盘展示个人考勤概览
     *
     * @param studentId 学生ID
     * @param startDate 统计开始日期（包含）
     * @param endDate   统计结束日期（包含）
     * @return 考勤统计视图对象（包含各类考勤状态的数量）
     */
    DashboardStatisticsVO.AttendanceStat getStatByStudentId(@Param("studentId") Long studentId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);
}