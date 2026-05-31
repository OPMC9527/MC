package com.edulink.controller;

import com.edulink.dto.AttendanceDTO;
import com.edulink.dto.AttendanceQueryDTO;
import com.edulink.service.AttendanceService;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import com.edulink.vo.AttendanceVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤记录控制器
 * 提供考勤记录的查询、签到、更新、删除等接口
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 根据考勤记录ID获取详情
     *
     * @param id 考勤记录ID
     * @return 考勤详情结果对象
     */
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return attendanceService.getAttendanceDetail(id);
    }

    /**
     * 分页查询考勤记录列表
     * 支持按学生、班级、日期范围等条件筛选
     *
     * @param queryDTO 查询条件及分页参数（pageNum、pageSize）
     * @return 分页结果，包含考勤记录列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<AttendanceVO>> list(AttendanceQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<AttendanceVO> list = attendanceService.listAttendances(queryDTO).getData();
        PageInfo<AttendanceVO> pageInfo = new PageInfo<>(list);
        PageResult<AttendanceVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 签到（新增考勤记录）
     * 使用 InsertGroup 校验分组
     *
     * @param dto 签到信息（学生ID、签到时间、签到状态等）
     * @return 操作结果
     */
    @PostMapping("/checkin")
    public Result<?> checkIn(@Validated(InsertGroup.class) @RequestBody AttendanceDTO dto) {
        return attendanceService.checkIn(dto);
    }

    /**
     * 更新考勤记录
     * 使用 UpdateGroup 校验分组（通常要求必须包含ID）
     *
     * @param dto 考勤记录更新信息（必须包含ID）
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@Validated(UpdateGroup.class) @RequestBody AttendanceDTO dto) {
        return attendanceService.updateAttendance(dto);
    }

    /**
     * 删除考勤记录
     *
     * @param id 考勤记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return attendanceService.deleteAttendance(id);
    }
}