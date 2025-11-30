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

@WebServlet("/checkPassword")
public class CheckPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String password = request.getParameter("password");
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("loginUser");

        boolean isValid = new UserDAO().checkPassword(user.getUserid(), password);
        
        JsonObject json = new JsonObject();
        json.addProperty("valid", isValid);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new Gson().toJson(json));
    }
}