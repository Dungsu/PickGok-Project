package com.pickgok.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.pickgok.user.dao.UserDAO;
import com.pickgok.user.model.UserDTO;

@WebServlet("/admin/members")
public class AdminMemberServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 조회: 회원 목록
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        
        // 관리자(admin) 체크
        if (user == null || !user.getUserid().equals("admin")) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        UserDAO dao = new UserDAO();
        List<UserDTO> userList = dao.getAllUsers();
        
        request.setAttribute("userList", userList);
        request.getRequestDispatcher("/views/admin/member_list.jsp").forward(request, response);
    }

    // 처리: 회원 삭제
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String deleteId = request.getParameter("deleteId");
        if(deleteId != null) {
            new UserDAO().deleteUser(deleteId);
        }
        response.sendRedirect("members"); // 목록 새로고침
    }
}