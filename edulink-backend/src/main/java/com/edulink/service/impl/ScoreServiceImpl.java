package com.edulink.service.impl;

import com.edulink.dto.ScoreQueryDTO;
import com.edulink.dto.ScoreSaveDTO;
import com.edulink.entity.Score;
import com.edulink.mapper.ScoreMapper;
import com.edulink.mapper.StudentMapper;
import com.edulink.service.ScoreService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.ScoreVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 成绩服务实现类
 * 提供成绩的增删改查、分页查询，并根据用户角色（学生/家长/教师/管理员）自动进行数据权限隔离
 */
@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 根据成绩ID查询成绩详情（包含学生、班级、教师关联信息）
     *
     * @param id 成绩记录ID
     * @return 成绩视图对象，包含学生姓名、班级名称、教师姓名等，不存在则返回 null
     */
    @Override
    public ScoreVO getScoreDetail(Long id) {
        return scoreMapper.selectScoreDetail(id);
    }

    /**
     * 条件查询成绩列表（支持分页，由调用方控制分页参数）
     * 根据当前登录用户角色自动设置查询范围：
     * - STUDENT：仅查询自己的成绩
     * - PARENT：查询其关联的所有子女的成绩
     * - ADMIN / TEACHER：查询所有成绩（无额外限制）
     *
     * @param queryDTO 查询条件对象（学生ID、班级ID、课程名称、考试类型等）
     * @return 成绩视图对象列表
     */
    @Override
    public List<ScoreVO> listScores(ScoreQueryDTO queryDTO) {
        String role = UserContext.getRole();
        Long currentUserId = UserContext.getUserId();

        if ("STUDENT".equals(role)) {
            // 学生：仅查询自己的成绩
            Long studentId = studentMapper.findIdByUserId(currentUserId);
            if (studentId != null) {
                queryDTO.setStudentId(studentId);
            } else {
                return new ArrayList<>();  // 当前用户不是学生，返回空列表
            }
        } else if ("PARENT".equals(role)) {
            // 家长：查询所有关联子女的成绩
            List<Long> studentIds = studentMapper.findIdsByParentId(currentUserId);
            if (!studentIds.isEmpty()) {
                queryDTO.setStudentIds(studentIds);
            } else {
                return new ArrayList<>();  // 没有关联的孩子，返回空列表
            }
        }
        // 教师和管理员不做额外限制，可查看所有成绩
        return scoreMapper.selectScoreList(queryDTO);
    }

    /**
     * 新增成绩记录
     * 自动填充教师ID（当前登录用户），如果前端未传递
     *
     * @param dto 成绩保存数据（学生ID、课程名称、分数、考试类型、考试日期等）
     * @return 操作结果
     */
    @Override
    public Result<?> addScore(ScoreSaveDTO dto) {
        Score score = new Score();
        BeanUtils.copyProperties(dto, score);

        // 自动填充 teacherId（记录该成绩的教师）
        if (score.getTeacherId() == null) {
            Long currentUserId = UserContext.getUserId();
            if (currentUserId == null) {
                return Result.error("未登录，无法获取教师信息");
            }
            score.setTeacherId(currentUserId);
        }

        scoreMapper.insert(score);
        return Result.success("添加成功");
    }

    /**
     * 更新成绩记录
     * 如果前端未传递 teacherId，则自动填充为当前登录用户ID
     *
     * @param dto 成绩保存数据（必须包含ID）
     * @return 操作结果
     */
    @Override
    public Result<?> updateScore(ScoreSaveDTO dto) {
        // 如果前端没有传递 teacherId，则从当前登录用户获取
        if (dto.getTeacherId() == null) {
            Long currentUserId = UserContext.getUserId();
            if (currentUserId == null) {
                return Result.error("未登录，无法获取教师信息");
            }
            dto.setTeacherId(currentUserId);
        }
        Score score = new Score();
        BeanUtils.copyProperties(dto, score);
        scoreMapper.update(score);
        return Result.success("更新成功");
    }

    /**
     * 删除成绩记录
     *
     * @param id 成绩记录ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<?> deleteScore(Long id) {
        int rows = scoreMapper.deleteById(id);
        if (rows == 0) {
            return Result.error("成绩记录不存在");
        }
        return Result.success("删除成功");
    }
}