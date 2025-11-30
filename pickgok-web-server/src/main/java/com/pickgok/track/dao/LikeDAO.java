package com.pickgok.track.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.pickgok.common.DBConnection;

public class LikeDAO {

    // 1. 좋아요 추가
    public boolean addLike(String userId, int trackId) {
        String sql = "INSERT IGNORE INTO likes (user_id, track_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, trackId);
            return pstmt.executeUpdate() > 0; 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. 좋아요 취소
    public boolean removeLike(String userId, int trackId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND track_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, trackId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. 좋아요 여부 확인
    public boolean isLiked(String userId, int trackId) {
        String sql = "SELECT 1 FROM likes WHERE user_id = ? AND track_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, trackId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. 행동 로그 기록 (LIKE / UNLIKE)
    public void recordHistory(String userId, int trackId, String actionType) {
        // DB Enum 값에 맞춰 대문자로 변환
        String dbAction = actionType.toUpperCase();
        // 유효성 검사 (DB 에러 방지)
        if (!"LIKE".equals(dbAction) && !"UNLIKE".equals(dbAction)) {
            dbAction = "UNLIKE"; // 기본값 처리
        }

        String sql = "INSERT INTO user_history (user_id, track_id, action_type) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setInt(2, trackId);
            pstmt.setString(3, dbAction);
            
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}