package com.pickgok.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson; // Gson Import 필수
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.pickgok.track.dao.TrackDAO;
import com.pickgok.track.model.TrackDTO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/recommend")
public class RecommendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // ★ Python Flask 서버 주소 (5000 포트)
    private static final String AI_SERVER_URL = "http://127.0.0.1:5000/recommend";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. 로그인 확인 및 사용자 정보 획득 (친구 코드와의 통합 포인트)
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("loginUser"); // ★ 세션 키값 확인 후 수정 필요
        
        if (user == null) {
            // response.sendRedirect("views/user/login.jsp"); // 로그인 화면으로 리다이렉트 (필요 시)
            // 테스트를 위해 임시로 ID 2번을 사용합니다.
             // return; 
        }

        // 2. 초기 Seed Track ID 설정 (테스트용 ID)
        String trackIdStr = request.getParameter("track_id");
        int seedTrackId = (trackIdStr != null && !trackIdStr.isEmpty()) ? Integer.parseInt(trackIdStr) : 2; 

        // [디버깅 1] 요청 시작
        System.out.println(">>> [DEBUG] 1. Request Start. Seed ID: " + seedTrackId);

        // Python AI 서버에 요청 보내기
        List<Integer> recommendedIds = fetchRecommendationsFromAI(seedTrackId);
        
        // [디버깅 2] Python 응답 확인
        System.out.println(">>> [DEBUG] 2. Python returned IDs: " + recommendedIds);

        if (recommendedIds.isEmpty()) {
            System.out.println(">>> [DEBUG] 🚨 Python returned EMPTY list. (Check Python Server Logic)");
        }

        // DB에서 상세 정보 조회
        TrackDAO dao = new TrackDAO();
        List<TrackDTO> recommendedTracks = dao.getTracksByIds(recommendedIds);

        // [디버깅 3] DB 조회 결과 확인
        System.out.println(">>> [DEBUG] 3. DB returned Tracks count: " + recommendedTracks.size());
        
        if (!recommendedIds.isEmpty() && recommendedTracks.isEmpty()) {
             System.out.println(">>> [DEBUG] 🚨 Critical: Python gave IDs, but DB found NOTHING. (Check DB Data)");
        }

        request.setAttribute("seedId", seedTrackId);
        request.setAttribute("recommendations", recommendedTracks);
        request.getRequestDispatcher("/views/recommend_result.jsp").forward(request, response);
    }

    private List<Integer> fetchRecommendationsFromAI(int trackId) {
        List<Integer> ids = new ArrayList<>();
        try {
        	URL url = URI.create(AI_SERVER_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // JSON 요청 본문 생성: {"track_id": 2, "k": 5}
            JsonObject jsonInput = new JsonObject();
            jsonInput.addProperty("track_id", trackId);
            jsonInput.addProperty("k", 5);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 읽기
            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                Gson gson = new Gson();
                JsonObject responseJson = gson.fromJson(br, JsonObject.class);
                JsonArray recArray = responseJson.getAsJsonArray("recommendations");
                
                for (int i = 0; i < recArray.size(); i++) {
                    JsonObject recItem = recArray.get(i).getAsJsonObject();
                    ids.add(recItem.get("track_id").getAsInt());
                }
            }
        } catch (Exception e) {
            System.err.println("AI Server Communication Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return ids;
    }
}