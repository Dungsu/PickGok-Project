package com.pickgok.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.pickgok.common.DBConnection;
import com.pickgok.user.model.UserDTO;

public class UserDAO {

    // [Helper] 유저 ID로 현재 DB의 비밀번호를 조회
    private String getCurrentPassword(String userid) {
        String sql = "SELECT password FROM users WHERE userid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userid);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 1. 회원가입
    public int join(UserDTO user) {
        String sql = "INSERT INTO users (userid, password, name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserid());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 2. 로그인 (수정됨)
    public UserDTO login(String userid, String password) {
        // preferred_genres 컬럼 추가 조회
        String sql = "SELECT userid, password, name, email, preferred_genres FROM users WHERE userid = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userid);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserid(rs.getString("userid"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    // [추가] 장르 정보 세팅 (NULL이면 빈 문자열)
                    String genres = rs.getString("preferred_genres");
                    user.setPreferredGenres(genres != null ? genres : "");
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. 정보 수정 (마이페이지) - 비밀번호 유지 로직 통합 및 결과 DTO 반환
    public UserDTO updateUserAndGet(UserDTO user) {
        // [핵심 로직]
        String finalPw = user.getPassword();
        String userId = user.getUserid();

        // 1. 새 비밀번호 필드가 비어있다면 (사용자가 변경을 원치 않음)
        if (finalPw == null || finalPw.trim().isEmpty()) {
            finalPw = getCurrentPassword(userId);
            if (finalPw == null) {
                return null; // 사용자를 찾을 수 없어 업데이트 불가
            }
        }
        
        // 2. 업데이트 실행 (비밀번호는 최종 결정된 finalPw 사용)
        String updateSql = "UPDATE users SET password = ?, name = ?, email = ? WHERE userid = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            
            pstmt.setString(1, finalPw); 
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, userId);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 3. 업데이트 성공 시 최신 정보 조회 (세션 갱신용)
                return getUserById(userId); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // [Helper] 유저 ID로 최신 UserDTO를 조회 (세션 갱신용)
    private UserDTO getUserById(String userId) {
        String sql = "SELECT userid, password, name, email FROM users WHERE userid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setUserid(rs.getString("userid"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. 전체 회원 조회 (관리자용)
    public List<UserDTO> getAllUsers() {
        List<UserDTO> list = new ArrayList<>();
        String sql = "SELECT userid, name, email, created_at FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UserDTO user = new UserDTO();
                user.setUserid(rs.getString("userid"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. 회원 삭제 (관리자용)
    public int deleteUser(String userid) {
        String sql = "DELETE FROM users WHERE userid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userid);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public boolean isIdExists(String userid) {
        String sql = "SELECT 1 FROM users WHERE userid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 데이터가 있으면 true
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // 에러 시 false 혹은 true로 처리 (정책에 따름)
    }

    // [추가] 이메일 중복 체크 (존재하면 true)
    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 // [추가] 선호 장르 업데이트
    public boolean updateGenres(String userId, String genres) {
        String sql = "UPDATE users SET preferred_genres = ? WHERE userid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genres);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // [추가] 비밀번호 확인 (정보 수정 접근용)
    public boolean checkPassword(String userId, String password) {
        String sql = "SELECT 1 FROM users WHERE userid = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}