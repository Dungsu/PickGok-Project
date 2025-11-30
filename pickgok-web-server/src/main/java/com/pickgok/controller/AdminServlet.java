package com.pickgok.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.pickgok.track.dao.TrackDAO;
import com.pickgok.track.model.TrackDTO;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // (원래는 여기서 관리자 세션 체크를 해야 합니다)

        // 랭킹 Top 10 가져오기
        TrackDAO dao = new TrackDAO();
        List<TrackDTO> topTracks = dao.getTopTracks(10);

        request.setAttribute("topTracks", topTracks);
        request.getRequestDispatcher("/views/admin/dashboard.jsp").forward(request, response);
    }
}