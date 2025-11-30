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
import com.pickgok.user.dao.UserDAO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/updateGenre")
public class GenreServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        
        JsonObject json = new JsonObject();
        response.setContentType("application/json;charset=UTF-8");

        if (user == null) {
            json.addProperty("status", "fail");
            response.getWriter().write(new Gson().toJson(json));
            return;
        }

        String genres = request.getParameter("genres"); // "Pop,Rock,Jazz"
        UserDAO dao = new UserDAO();
        boolean result = dao.updateGenres(user.getUserid(), genres);

        if (result) {
            // [추가] DB 업데이트 성공 시, 현재 세션의 유저 정보도 갱신
            user.setPreferredGenres(genres); 
            session.setAttribute("loginUser", user);
            
            json.addProperty("status", "success");
        } else {
            json.addProperty("status", "fail");
        }
        response.getWriter().write(new Gson().toJson(json));
    }
}