package com.pickgok.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.pickgok.track.dao.TrackDAO;

@WebServlet("/count")
public class PlayCountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String trackIdStr = request.getParameter("track_id");
        if (trackIdStr != null) {
            try {
                int trackId = Integer.parseInt(trackIdStr);
                new TrackDAO().incrementPlayCount(trackId);
                // System.out.println("Count up for track: " + trackId); // 확인용 로그
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 응답 본문 없이 성공 코드(200)만 반환
        response.setStatus(HttpServletResponse.SC_OK);
    }
}