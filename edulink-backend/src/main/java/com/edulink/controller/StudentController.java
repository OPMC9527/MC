package com.edulink.controller;

import com.edulink.dto.StudentQueryDTO;
import com.edulink.dto.StudentUpdateDTO;
import com.edulink.service.StudentService;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import com.edulink.vo.StudentVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生信息控制器
 * 提供学生信息的查询、新增、更新、删除以及分页列表接口
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 根据学生ID获取学生详情
     *
     * @param id 学生ID
     * @return 学生详情结果对象
     */
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return studentService.getStudentDetail(id);
    }

    /**
     * 分页查询学生列表
     * 支持按姓名、学号、班级、状态等条件筛选
     *
     * @param queryDTO 查询条件及分页参数（pageNum、pageSize）
     * @return 分页结果，包含学生信息列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<StudentVO>> list(StudentQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<StudentVO> list = studentService.listStudents(queryDTO);
        PageInfo<StudentVO> pageInfo = new PageInfo<>(list);
        PageResult<StudentVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 新增学生
     * 使用 InsertGroup 校验分组，此时 id 字段不会校验（id 由数据库自动生成）
     *
     * @param dto 学生信息（不包含ID）
     * @return 操作结果
     */
    @PostMapping
    public Result<?> add(@Validated(InsertGroup.class) @RequestBody StudentUpdateDTO dto) {
        return studentService.addStudent(dto);
    }

    /**
     * 更新学生信息
     * 使用 UpdateGroup 校验分组，此时 id 字段必须存在且不为空
     *
     * @param dto 学生信息（必须包含ID）
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@Validated(UpdateGroup.class) @RequestBody StudentUpdateDTO dto) {
        return studentService.updateStudent(dto);
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return studentService.deleteStudent(id);
    }
}