package com.edulink.service.impl;

import com.edulink.dto.NoticeDTO;
import com.edulink.dto.NoticeQueryDTO;
import com.edulink.entity.Notice;
import com.edulink.mapper.NoticeMapper;
import com.edulink.service.NoticeService;
import com.edulink.utils.Result;
import com.edulink.utils.UserContext;
import com.edulink.vo.NoticeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知公告服务实现类
 * 提供通知的发布、查询、更新、删除以及浏览次数自增等功能
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 获取通知详情
     * 每次查看时自动增加浏览次数（view_count）
     *
     * @param id 通知ID
     * @return 通知视图对象，包含标题、内容、发布人、浏览次数等，不存在则返回 null
     */
    @Override
    public NoticeVO getNoticeDetail(Long id) {
        NoticeVO vo = noticeMapper.selectById(id);
        if (vo != null) {
            // 浏览次数加1
            noticeMapper.incrementViewCount(id);
        }
        return vo;  // 如果为 null 则由 Controller 处理并返回错误信息
    }

    /**
     * 条件查询通知列表（支持分页）
     *
     * @param queryDTO 查询条件对象（标题模糊搜索、紧急程度筛选、分页参数）
     * @return 通知视图对象列表
     */
    @Override
    public List<NoticeVO> listNotices(NoticeQueryDTO queryDTO) {
        String role = UserContext.getRole();
        // 管理员和教师可以看到所有通知（不限制角色）
        if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
            return noticeMapper.selectList(queryDTO.getTitle(),
                    queryDTO.getIsUrgent(), null);
        } else {
            // 学生和家长只能看到包含自己角色或 target_roles 为空的
            return noticeMapper.selectList(queryDTO.getTitle(),
                    queryDTO.getIsUrgent(), role);
        }
    }

    /**
     * 发布新通知
     *
     * @param dto         通知数据（标题、内容、目标角色、紧急程度等）
     * @param publisherId 发布人ID（从当前登录用户获取）
     * @return 操作结果
     */
    @Override
    public Result<?> publishNotice(NoticeDTO dto, Long publisherId) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setPublisherId(publisherId);
        noticeMapper.insert(notice);
        return Result.success("发布成功");
    }

    /**
     * 更新通知信息
     * 可更新标题、内容、目标角色、紧急程度
     *
     * @param dto 通知更新数据（必须包含ID）
     * @return 操作结果
     */
    @Override
    public Result<?> updateNotice(NoticeDTO dto) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        noticeMapper.update(notice);
        return Result.success("更新成功");
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Override
    public Result<?> deleteNotice(Long id) {
        noticeMapper.deleteById(id);
        return Result.success("删除成功");
    }
}