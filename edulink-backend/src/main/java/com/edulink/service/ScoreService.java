package com.edulink.service;

import com.edulink.dto.ScoreQueryDTO;
import com.edulink.dto.ScoreSaveDTO;
import com.edulink.utils.Result;
import com.edulink.vo.ScoreVO;

import java.util.List;

public interface ScoreService {
    ScoreVO getScoreDetail(Long id);
    List<ScoreVO> listScores(ScoreQueryDTO queryDTO);
    Result<?> addScore(ScoreSaveDTO dto);
    Result<?> updateScore(ScoreSaveDTO dto);
    Result<?> deleteScore(Long id);
}