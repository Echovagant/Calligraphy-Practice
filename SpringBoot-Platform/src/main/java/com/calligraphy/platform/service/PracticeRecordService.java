package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.PracticeRecordDto;
import com.calligraphy.platform.dto.PracticeRecordCreateDto;
import com.calligraphy.platform.dto.PracticeRecordUpdateDto;
import com.calligraphy.platform.model.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PracticeRecordService {
    
    PracticeRecordDto createPracticeRecord(String userId, PracticeRecordCreateDto createDto);
    
    PracticeRecordDto getPracticeRecordById(String recordId);
    
    Optional<PracticeRecord> getPracticeRecordEntityById(String recordId);
    
    Page<PracticeRecordDto> getAllPracticeRecords(Pageable pageable);
    
    List<PracticeRecordDto> getAllPracticeRecords();
    
    Page<PracticeRecordDto> getPracticeRecordsByUserId(String userId, Pageable pageable);
    
    List<PracticeRecordDto> getPracticeRecordsByUserId(String userId);
    
    Page<PracticeRecordDto> getPracticeRecordsByCharacter(String character, Pageable pageable);
    
    List<PracticeRecordDto> getPracticeRecordsByCharacter(String character);
    
    Page<PracticeRecordDto> getPracticeRecordsByFontStyle(String fontStyle, Pageable pageable);
    
    List<PracticeRecordDto> getPracticeRecordsByFontStyle(String fontStyle);
    
    Page<PracticeRecordDto> getPracticeRecordsByDifficultyLevel(String difficultyLevel, Pageable pageable);
    
    List<PracticeRecordDto> getPracticeRecordsByDifficultyLevel(String difficultyLevel);
    
    Page<PracticeRecordDto> getPracticeRecordsByCompletionStatus(String status, Pageable pageable);
    
    List<PracticeRecordDto> getPracticeRecordsByCompletionStatus(String status);
    
    PracticeRecordDto updatePracticeRecord(String recordId, String userId, PracticeRecordUpdateDto updateDto);
    
    PracticeRecordDto updatePracticeScore(String recordId, String userId, Integer score);
    
    PracticeRecordDto updateCompletionStatus(String recordId, String userId, String status);
    
    PracticeRecordDto updateSatisfactionRating(String recordId, String userId, Integer rating);
    
    void deletePracticeRecord(String recordId, String userId);
    
    // 搜索和筛选
    Page<PracticeRecordDto> searchPracticeRecords(String keyword, Pageable pageable);
    
    List<PracticeRecordDto> searchPracticeRecords(String keyword);
    
    // 统计相关
    long getTotalPracticeRecordCount();
    
    long getPracticeRecordCountByUserId(String userId);
    
    long getPracticeRecordCountByCharacter(String character);
    
    long getPracticeRecordCountByFontStyle(String fontStyle);
    
    long getPracticeRecordCountByDifficultyLevel(String difficultyLevel);
    
    long getPracticeRecordCountByCompletionStatus(String status);
    
    long getPracticeRecordCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 用户练习统计
    long getUserTotalPracticeCount(String userId);
    
    long getUserCompletedPracticeCount(String userId);
    
    long getUserInProgressPracticeCount(String userId);
    
    double getUserAverageScore(String userId);
    
    double getUserAverageCompletionRate(String userId);
    
    // 字符练习统计
    long getCharacterPracticeCount(String character);
    
    double getCharacterAverageScore(String character);
    
    double getCharacterCompletionRate(String character);
    
    // 字体练习统计
    long getFontStylePracticeCount(String fontStyle);
    
    double getFontStyleAverageScore(String fontStyle);
    
    double getFontStyleCompletionRate(String fontStyle);
    
    // 难度级别统计
    long getDifficultyLevelPracticeCount(String difficultyLevel);
    
    double getDifficultyLevelAverageScore(String difficultyLevel);
    
    double getDifficultyLevelCompletionRate(String difficultyLevel);
    
    // 最近练习
    List<PracticeRecordDto> getRecentPracticeRecords(int limit);
    
    List<PracticeRecordDto> getRecentPracticeRecordsByUserId(String userId, int limit);
    
    // 最高分和最佳表现
    List<PracticeRecordDto> getTopScoringPracticeRecords(int limit);
    
    List<PracticeRecordDto> getTopScoringPracticeRecordsByUserId(String userId, int limit);
    
    List<PracticeRecordDto> getTopScoringPracticeRecordsByCharacter(String character, int limit);
    
    // 练习趋势分析
    List<PracticeRecordDto> getPracticeRecordsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<PracticeRecordDto> getUserPracticeRecordsByDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 批量操作
    void bulkDeletePracticeRecords(List<String> recordIds);
    
    void bulkUpdateCompletionStatus(List<String> recordIds, String status);
    
    // 练习建议
    List<String> getRecommendedCharactersForUser(String userId);
    
    List<String> getRecommendedFontStylesForUser(String userId);
    
    String getRecommendedDifficultyLevelForUser(String userId);
    
    // 练习进度
    double getUserOverallProgress(String userId);
    
    double getUserCharacterProgress(String userId, String character);
    
    double getUserFontStyleProgress(String userId, String fontStyle);
}


