package com.pickgok.controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.pickgok.user.dao.UserDAO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String id = request.getParameter("userid");
        String pw = request.getParameter("password");
        
        UserDAO dao = new UserDAO();
        UserDTO user = dao.login(id, pw);
        
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user); // 세션 생성
            response.sendRedirect("home"); 
        } else {
            out.println("<script>alert('아이디 또는 비밀번호가 일치하지 않습니다.'); history.back();</script>");
        }
    }
}