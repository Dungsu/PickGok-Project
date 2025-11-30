package com.pickgok.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.pickgok.track.dao.TrackDAO;
import com.pickgok.track.model.TrackDTO;

// 사용자가 http://localhost:8080/PickGok/home 으로 접속하면 실행됨
@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. DAO를 통해 랜덤 트랙 1곡 가져오기
        TrackDAO dao = new TrackDAO();
        TrackDTO track = dao.getRandomTrack();
        
        // 2. 가져온 트랙 정보를 request 영역에 저장 ("track"이라는 이름표를 붙임)
        if (track != null) {
            request.setAttribute("track", track);
        }
        
        // 3. 데이터를 가지고 화면(index.jsp)으로 이동
        request.getRequestDispatcher("/views/home.jsp").forward(request, response);
    }
}