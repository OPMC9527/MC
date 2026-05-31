package com.edulink.controller;

import com.edulink.entity.Class;
import com.edulink.mapper.ClassMapper;
import com.edulink.service.ClassService;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.vo.ClassVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级控制器
 * 提供班级管理的相关接口：分页查询、根据ID查询、新增、更新、删除班级
 */
@RestController
@RequestMapping("/class")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private ClassMapper classMapper;

    /**
     * 分页查询班级列表（支持按班级名称和年级筛选）
     *
     * @param pageNum   当前页码，默认为1
     * @param pageSize  每页记录数，默认为10
     * @param className 班级名称（可选，模糊匹配）
     * @param grade     年级（可选，如“高一”、“二年级”）
     * @return 分页结果，包含班级信息列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<ClassVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String className,
                                            @RequestParam(required = false) String grade) {
        PageHelper.startPage(pageNum, pageSize);
        List<ClassVO> list = classService.selectList(className, grade);
        if (list == null) list = new ArrayList<>();
        PageInfo<ClassVO> pageInfo = new PageInfo<>(list);
        PageResult<ClassVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 根据班级ID查询班级详情
     *
     * @param id 班级ID
     * @return 班级实体，若不存在则返回错误信息
     */
    @GetMapping("/{id}")
    public Result<Class> getById(@PathVariable Long id) {
        return classService.getById(id);
    }

    /**
     * 新增班级
     *
     * @param clazz 班级实体（需包含班级名称、年级等必填字段）
     * @return 操作结果，成功返回新增的班级信息
     */
    @PostMapping
    public Result<?> add(@Valid @RequestBody Class clazz) {
        return classService.addClass(clazz);
    }

    /**
     * 更新班级信息
     *
     * @param clazz 班级实体（必须包含ID，其他字段按需更新）
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@Valid @RequestBody Class clazz) {
        return classService.updateClass(clazz);
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 操作结果，删除前应检查是否存在关联的学生数据
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return classService.deleteClass(id);
    }
}