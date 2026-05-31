package com.edulink.service;

import com.edulink.dto.StudentQueryDTO;
import com.edulink.dto.StudentUpdateDTO;
import com.edulink.utils.Result;
import com.edulink.vo.StudentVO;

import java.util.List;

public interface StudentService {
    Result<StudentVO> getStudentDetail(Long id);
    Result<?> addStudent(StudentUpdateDTO dto);
    Result<?> updateStudent(StudentUpdateDTO dto);
    Result<?> deleteStudent(Long id);
    List<StudentVO> listStudents(StudentQueryDTO queryDTO);

}