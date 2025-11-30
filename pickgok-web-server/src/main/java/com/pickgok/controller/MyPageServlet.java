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

@WebServlet("/mypage")
public class MyPageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 조회: 마이페이지로 이동
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginUser") == null) {
            response.sendRedirect("views/user/login.jsp");
            return;
        }
        // JSP로 포워딩 (여기까지는 그대로 유지)
        request.getRequestDispatcher("views/user/mypage.jsp").forward(request, response);
    }

    // 수정: 정보 업데이트 처리 (리팩토링)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        UserDTO sessionUser = (UserDTO) session.getAttribute("loginUser"); // 세션에서 기존 정보 가져옴
        
        // 1. 로그인 체크
        if (sessionUser == null) {
             response.sendRedirect("views/user/login.jsp");
             return;
        }

        // 2. 파라미터 받기
        String newPw = request.getParameter("password");
        String newName = request.getParameter("name");
        String newEmail = request.getParameter("email");
        
        // 3. 업데이트 DTO 준비 (세션 정보를 기반으로 업데이트)
        UserDTO updateUser = new UserDTO();
        updateUser.setUserid(sessionUser.getUserid());
        updateUser.setName(newName);
        updateUser.setEmail(newEmail);

        // ★ 비밀번호 로직: 비어있으면 기존 비밀번호 사용 (DAO에서 처리)
        if (newPw != null && !newPw.trim().isEmpty()) {
            // (운영 환경이라면 여기서 새 비밀번호를 해시화해야 함)
            updateUser.setPassword(newPw); 
        } else {
            // 비밀번호가 비어있다면, DAO가 기존 비밀번호를 그대로 사용하도록 플래그를 설정 (null 또는 특수 값)
            // 이를 위해 UserDAO에 별도의 플래그나 로직이 필요함.
            updateUser.setPassword(null); 
        }
        
     // 4. DAO 호출 및 세션 갱신
        UserDAO dao = new UserDAO();

        // UserDAO의 메서드 이름을 updateUserAndGet으로 변경합니다.
        UserDTO resultUser = dao.updateUserAndGet(updateUser); // ★ 이 부분이 수정된 부분

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (resultUser != null) {
            session.setAttribute("loginUser", resultUser); // DAO에서 받은 최신 정보로 세션 갱신
            out.println("<script>alert('정보가 수정되었습니다.'); location.href='mypage';</script>");
        } else {
            out.println("<script>alert('수정 실패.'); history.back();</script>");
        }
    }
    }
    