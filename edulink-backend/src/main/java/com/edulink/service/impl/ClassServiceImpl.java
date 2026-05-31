package com.edulink.service.impl;

import com.edulink.entity.Class;
import com.edulink.mapper.ClassMapper;
import com.edulink.service.ClassService;
import com.edulink.utils.Result;
import com.edulink.vo.ClassVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务实现类
 * 提供班级的增删改查功能，以及分页条件查询
 */
@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    /**
     * 查询所有班级（不分页）
     *
     * @return 包含所有班级列表的结果对象
     */
    @Override
    public Result<List<Class>> listAll() {
        return Result.success(classMapper.selectAll());
    }

    /**
     * 根据ID查询班级详情
     *
     * @param id 班级ID
     * @return 班级实体，若不存在则返回错误信息
     */
    @Override
    public Result<Class> getById(Long id) {
        Class clazz = classMapper.selectById(id);
        return clazz != null ? Result.success(clazz) : Result.error("班级不存在");
    }

    /**
     * 新增班级
     *
     * @param clazz 班级实体（不含ID，ID自动生成）
     * @return 操作结果
     */
    @Override
    public Result<?> addClass(Class clazz) {
        classMapper.insert(clazz);
        return Result.success("添加成功");
    }

    /**
     * 更新班级信息
     *
     * @param clazz 班级实体（必须包含ID）
     * @return 操作结果
     */
    @Override
    public Result<?> updateClass(Class clazz) {
        classMapper.update(clazz);
        return Result.success("更新成功");
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 操作结果（注意：物理删除，需确认无关联学生数据）
     */
    @Override
    public Result<?> deleteClass(Long id) {
        classMapper.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 条件查询班级列表（支持分页，由调用方控制分页参数）
     * 若查询结果为null则返回空列表，避免前端NPE
     *
     * @param className 班级名称（模糊匹配）
     * @param grade     年级（精确匹配）
     * @return 班级视图对象列表（包含班主任姓名）
     */
    @Override
    public List<ClassVO> selectList(String className, String grade) {
        List<ClassVO> list = classMapper.selectList(className, grade);
        return list == null ? new ArrayList<>() : list;
    }
}