package com.edulink.service;

import com.edulink.dto.NoticeDTO;
import com.edulink.dto.NoticeQueryDTO;
import com.edulink.utils.Result;
import com.edulink.vo.NoticeVO;
import java.util.List;

public interface NoticeService {
    NoticeVO getNoticeDetail(Long id);  // 改为返回 NoticeVO
    List<NoticeVO> listNotices(NoticeQueryDTO queryDTO);  // 返回 List
    Result<?> publishNotice(NoticeDTO dto, Long publisherId);
    Result<?> updateNotice(NoticeDTO dto);
    Result<?> deleteNotice(Long id);
}