package com.edulink.controller;

import com.edulink.dto.ScoreQueryDTO;
import com.edulink.dto.ScoreSaveDTO;
import com.edulink.service.ScoreService;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.vo.ScoreVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 成绩管理控制器
 * 提供成绩的增删改查、分页查询等接口
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    /**
     * 根据成绩ID查询详情
     *
     * @param id 成绩记录ID
     * @return 成绩详情结果对象，不存在则返回错误信息
     */
    @GetMapping("/{id}")
    public Result<ScoreVO> getDetail(@PathVariable Long id) {
        ScoreVO vo = scoreService.getScoreDetail(id);
        if (vo == null) {
            return Result.error("成绩记录不存在");
        }
        return Result.success(vo);
    }

    /**
     * 分页查询成绩列表（支持条件筛选）
     * 可按学生、班级、课程、考试类型、分数区间等条件查询
     *
     * @param queryDTO 查询条件及分页参数（pageNum、pageSize）
     * @return 分页结果，包含成绩列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<ScoreVO>> list(ScoreQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<ScoreVO> list = scoreService.listScores(queryDTO);
        PageInfo<ScoreVO> pageInfo = new PageInfo<>(list);
        PageResult<ScoreVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 新增成绩记录
     *
     * @param dto 成绩保存数据（学生ID、课程ID、分数、考试名称等）
     * @return 操作结果
     */
    @PostMapping
    public Result<?> add(@Valid @RequestBody ScoreSaveDTO dto) {
        return scoreService.addScore(dto);
    }

    /**
     * 修改成绩记录
     *
     * @param dto 成绩保存数据（必须包含ID）
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@Valid @RequestBody ScoreSaveDTO dto) {
        return scoreService.updateScore(dto);
    }

    /**
     * 删除成绩记录
     *
     * @param id 成绩记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return scoreService.deleteScore(id);
    }
}