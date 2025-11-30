package com.pickgok.track.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pickgok.common.DBConnection;
import com.pickgok.track.model.TrackDTO;

public class TrackDAO {

    // 공통: ResultSet -> TrackDTO 매핑 헬퍼 메서드
    private TrackDTO mapToDTO(ResultSet rs) throws SQLException {
        TrackDTO dto = new TrackDTO();
        dto.setTrackId(rs.getInt("track_id"));
        dto.setTitle(rs.getString("title"));
        dto.setArtist(rs.getString("artist"));
        dto.setGenre(rs.getString("genre"));
        dto.setFilePath(rs.getString("file_path"));
        // dto.setDuration(rs.getInt("duration")); // 필요시 주석 해제
        return dto;
    }

    /**
     * AI 서버에서 받은 ID 리스트로 DB에서 상세 정보를 조회합니다.
     * [중요] AI가 추천한 '순서'를 유지하여 반환합니다.
     */
    public List<TrackDTO> getTracksByIds(List<Integer> trackIds) {
        List<TrackDTO> resultList = new ArrayList<>();
        if (trackIds == null || trackIds.isEmpty()) return resultList;

        // 1. 동적 쿼리 생성
        StringBuilder sql = new StringBuilder("SELECT track_id, title, artist, genre, file_path, duration FROM tracks WHERE track_id IN (");
        for (int i = 0; i < trackIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        // 2. DB 조회 및 Map에 임시 저장
        Map<Integer, TrackDTO> tempMap = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < trackIds.size(); i++) {
                pstmt.setInt(i + 1, trackIds.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TrackDTO dto = mapToDTO(rs);
                    tempMap.put(dto.getTrackId(), dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. AI가 요청한 순서대로 리스트 재조립 (Re-ordering)
        for (Integer id : trackIds) {
            if (tempMap.containsKey(id)) {
                resultList.add(tempMap.get(id));
            }
        }

        return resultList;
    }

    /**
     * 메인 화면용: DB에서 랜덤하게 트랙 1개를 가져옵니다.
     */
    public TrackDTO getRandomTrack() {
        String sql = "SELECT track_id, title, artist, genre, file_path, duration FROM tracks ORDER BY RAND() LIMIT 1";
        TrackDTO track = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                track = mapToDTO(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return track;
    }
    
 // [추가 1] 재생 횟수 1 증가 (조회수 카운팅)
    public void incrementPlayCount(int trackId) {
        String sql = "UPDATE tracks SET play_count = play_count + 1 WHERE track_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trackId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // [추가 2] 랭킹 조회 (Top N)
    public List<TrackDTO> getTopTracks(int limit) {
        List<TrackDTO> list = new ArrayList<>();
        // 재생수 내림차순 정렬
        String sql = "SELECT track_id, title, artist, genre, file_path, play_count FROM tracks ORDER BY play_count DESC LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TrackDTO dto = new TrackDTO(); // mapToDTO 메서드 있으면 그거 쓰셔도 됩니다
                    dto.setTrackId(rs.getInt("track_id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setArtist(rs.getString("artist"));
                    dto.setGenre(rs.getString("genre"));
                    dto.setFilePath(rs.getString("file_path"));
                    // playCount 필드가 DTO에 없다면 추가하거나, 지금은 순서대로 담기니까 생략 가능
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}