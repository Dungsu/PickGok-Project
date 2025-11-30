package com.pickgok.controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.pickgok.user.dao.UserDAO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/join")
public class JoinServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        UserDTO user = new UserDTO();
        String id = request.getParameter("userid");
        String email = request.getParameter("email");
        
        // [보안] 서버 측 중복 검사 (JS 우회 방지)
        UserDAO dao = new UserDAO();
        if (dao.isIdExists(id) || dao.isEmailExists(email)) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<script>alert('이미 존재하는 아이디 또는 이메일입니다.'); history.back();</script>");
            return;
        }

        user.setUserid(id);
        user.setPassword(request.getParameter("password"));
        user.setName(request.getParameter("name"));
        user.setEmail(email);
        
        // 회원가입 실행
        int result = dao.join(user); 
        
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        if (result > 0) {
            // 가입 성공 후 선호 장르 선택 페이지가 있다면 거기로, 없다면 로그인으로
            out.println("<script>alert('환영합니다! 로그인해주세요.'); location.href='views/user/login.jsp';</script>");
        } else {
            out.println("<script>alert('서버 오류로 가입에 실패했습니다.'); history.back();</script>");
        }
    }
}