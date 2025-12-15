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
    
    // 鎼滅储鍜岀瓫閫?    Page<PracticeRecordDto> searchPracticeRecords(String keyword, Pageable pageable);
    
    List<PracticeRecordDto> searchPracticeRecords(String keyword);
    
    // 缁熻鐩稿叧
    long getTotalPracticeRecordCount();
    
    long getPracticeRecordCountByUserId(String userId);
    
    long getPracticeRecordCountByCharacter(String character);
    
    long getPracticeRecordCountByFontStyle(String fontStyle);
    
    long getPracticeRecordCountByDifficultyLevel(String difficultyLevel);
    
    long getPracticeRecordCountByCompletionStatus(String status);
    
    long getPracticeRecordCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鐢ㄦ埛缁冧範缁熻
    long getUserTotalPracticeCount(String userId);
    
    long getUserCompletedPracticeCount(String userId);
    
    long getUserInProgressPracticeCount(String userId);
    
    double getUserAverageScore(String userId);
    
    double getUserAverageCompletionRate(String userId);
    
    // 瀛楃缁冧範缁熻
    long getCharacterPracticeCount(String character);
    
    double getCharacterAverageScore(String character);
    
    double getCharacterCompletionRate(String character);
    
    // 瀛椾綋缁冧範缁熻
    long getFontStylePracticeCount(String fontStyle);
    
    double getFontStyleAverageScore(String fontStyle);
    
    double getFontStyleCompletionRate(String fontStyle);
    
    // 闅惧害绾у埆缁熻
    long getDifficultyLevelPracticeCount(String difficultyLevel);
    
    double getDifficultyLevelAverageScore(String difficultyLevel);
    
    double getDifficultyLevelCompletionRate(String difficultyLevel);
    
    // 鏈€杩戠粌涔?    List<PracticeRecordDto> getRecentPracticeRecords(int limit);
    
    List<PracticeRecordDto> getRecentPracticeRecordsByUserId(String userId, int limit);
    
    // 鏈€楂樺垎鍜屾渶浣宠〃鐜?    List<PracticeRecordDto> getTopScoringPracticeRecords(int limit);
    
    List<PracticeRecordDto> getTopScoringPracticeRecordsByUserId(String userId, int limit);
    
    List<PracticeRecordDto> getTopScoringPracticeRecordsByCharacter(String character, int limit);
    
    // 缁冧範瓒嬪娍鍒嗘瀽
    List<PracticeRecordDto> getPracticeRecordsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<PracticeRecordDto> getUserPracticeRecordsByDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鎵归噺鎿嶄綔
    void bulkDeletePracticeRecords(List<String> recordIds);
    
    void bulkUpdateCompletionStatus(List<String> recordIds, String status);
    
    // 缁冧範寤鸿
    List<String> getRecommendedCharactersForUser(String userId);
    
    List<String> getRecommendedFontStylesForUser(String userId);
    
    String getRecommendedDifficultyLevelForUser(String userId);
    
    // 缁冧範杩涘害
    double getUserOverallProgress(String userId);
    
    double getUserCharacterProgress(String userId, String character);
    
    double getUserFontStyleProgress(String userId, String fontStyle);
}


