package com.pickgok.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pickgok.user.dao.UserDAO;

@WebServlet("/checkDuplicate")
public class CheckDuplicateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 요청 타입(id 또는 email)과 값 받기
        String type = request.getParameter("type");
        String value = request.getParameter("value");
        
        UserDAO dao = new UserDAO();
        boolean exists = false;
        
        if ("id".equals(type)) {
            exists = dao.isIdExists(value);
        } else if ("email".equals(type)) {
            exists = dao.isEmailExists(value);
        }
        
        // JSON 응답
        response.setContentType("application/json;charset=UTF-8");
        JsonObject json = new JsonObject();
        json.addProperty("exists", exists);
        
        response.getWriter().write(new Gson().toJson(json));
    }
}