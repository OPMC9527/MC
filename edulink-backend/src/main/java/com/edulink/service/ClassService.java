package com.edulink.service;

import com.edulink.entity.Class;
import com.edulink.utils.Result;
import com.edulink.vo.ClassVO;

import java.util.List;

public interface ClassService {
    Result<List<Class>> listAll();
    Result<Class> getById(Long id);
    Result<?> addClass(Class clazz);
    Result<?> updateClass(Class clazz);
    Result<?> deleteClass(Long id);

    List<ClassVO> selectList(String className, String grade);
}