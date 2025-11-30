package com.pickgok.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pickgok.track.dao.LikeDAO;
import com.pickgok.track.dao.TrackDAO;
import com.pickgok.track.model.TrackDTO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/like")
public class LikeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String LOGIN_SESSION_KEY = "loginUser"; 

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        JsonObject jsonResponse = new JsonObject();
        
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute(LOGIN_SESSION_KEY);
        
        if (user == null) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "로그인이 필요한 서비스입니다.");
            response.getWriter().write(new Gson().toJson(jsonResponse));
            return;
        }

        String trackIdStr = request.getParameter("track_id");
        String action = request.getParameter("action"); 
        // action: "add"(=LIKE), "skip"(=UNLIKE), "remove"(=취소)

        if (trackIdStr != null && action != null) {
            try {
                int trackId = Integer.parseInt(trackIdStr);
                LikeDAO likeDao = new LikeDAO();
                boolean success = true; 
                
                // 1. 액션 처리 및 히스토리 기록
                if ("add".equals(action) || "like".equals(action)) {
                    // 좋아요: 보관함 저장 + 히스토리(LIKE)
                    likeDao.addLike(user.getUserid(), trackId);
                    likeDao.recordHistory(user.getUserid(), trackId, "LIKE");
                } 
                else if ("skip".equals(action) || "unlike".equals(action)) {
                    // 싫어요/넘기기: 히스토리(UNLIKE)만 기록
                    // DB ENUM이 'UNLIKE'이므로 여기서 변환해서 넣어줌
                    likeDao.recordHistory(user.getUserid(), trackId, "UNLIKE");
                    success = true; 
                } 
                else if ("remove".equals(action)) {
                    // 좋아요 취소: 보관함 삭제 (히스토리 기록 안 함 or 별도 처리)
                    success = likeDao.removeLike(user.getUserid(), trackId);
                }
                
                if (success) {
                    jsonResponse.addProperty("status", "success");
                    
                    // 2. 다음 곡 정보 반환 (취소인 경우 제외)
                    if (!"remove".equals(action)) {
                        TrackDAO trackDao = new TrackDAO();
                        TrackDTO nextTrack = trackDao.getRandomTrack();
                        
                        if (nextTrack != null) {
                            JsonObject trackJson = new JsonObject();
                            trackJson.addProperty("trackId", nextTrack.getTrackId());
                            trackJson.addProperty("title", nextTrack.getTitle());
                            trackJson.addProperty("artist", nextTrack.getArtist());
                            trackJson.addProperty("filePath", nextTrack.getFilePath());
                            
                            jsonResponse.add("nextTrack", trackJson);
                        }
                    }
                } else {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "DB 처리 실패");
                }
            } catch (Exception e) {
                 jsonResponse.addProperty("status", "error");
                 e.printStackTrace();
            }
        } else {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "잘못된 요청");
        }
        
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}