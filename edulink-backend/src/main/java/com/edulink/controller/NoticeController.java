package com.edulink.controller;

import com.edulink.dto.NoticeDTO;
import com.edulink.dto.NoticeQueryDTO;
import com.edulink.service.NoticeService;
import com.edulink.utils.JwtUtil;
import com.edulink.utils.PageResult;
import com.edulink.utils.Result;
import com.edulink.validator.InsertGroup;
import com.edulink.validator.UpdateGroup;
import com.edulink.vo.NoticeVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告控制器
 * 提供通知的发布、查询、更新、删除等接口
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 根据通知ID获取通知详情
     *
     * @param id 通知ID
     * @return 通知详情结果对象，不存在则返回错误信息
     */
    @GetMapping("/{id}")
    public Result<NoticeVO> getDetail(@PathVariable Long id) {
        NoticeVO vo = noticeService.getNoticeDetail(id);
        if (vo == null) {
            return Result.error("通知不存在");
        }
        return Result.success(vo);
    }

    /**
     * 分页查询通知列表
     * 支持按标题、发布人、发布时间范围等条件筛选
     *
     * @param queryDTO 查询条件及分页参数（pageNum、pageSize）
     * @return 分页结果，包含通知列表及总记录数
     */
    @GetMapping("/list")
    public Result<PageResult<NoticeVO>> list(NoticeQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<NoticeVO> list = noticeService.listNotices(queryDTO);
        PageInfo<NoticeVO> pageInfo = new PageInfo<>(list);
        PageResult<NoticeVO> pageResult = new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
        return Result.success(pageResult);
    }

    /**
     * 发布新通知
     * 需要提供有效的JWT令牌，从令牌中解析发布人ID
     *
     * @param dto   通知数据（标题、内容、目标角色等）
     * @param token Authorization头部的JWT令牌
     * @return 操作结果
     */
    @PostMapping
    public Result<?> publish(@Validated(InsertGroup.class) @RequestBody NoticeDTO dto,
                             @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserId(token);
        return noticeService.publishNotice(dto, userId);
    }

    /**
     * 更新通知
     * 使用 UpdateGroup 校验分组（要求必须包含ID）
     *
     * @param dto 通知更新数据（必须包含ID）
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@Validated(UpdateGroup.class) @RequestBody NoticeDTO dto) {
        return noticeService.updateNotice(dto);
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return noticeService.deleteNotice(id);
    }
}