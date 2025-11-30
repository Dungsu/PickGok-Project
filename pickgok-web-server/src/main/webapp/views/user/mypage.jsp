<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.pickgok.user.model.UserDTO"%>
<%
    UserDTO user = (UserDTO) session.getAttribute("loginUser");
    if(user == null) { response.sendRedirect("login.jsp"); return; }
    
    String savedGenres = user.getPreferredGenres();
    if(savedGenres == null) savedGenres = "";
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>마이페이지 - PickGok</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/mypage.css">

    <script>
        (function() {
            const savedTheme = localStorage.getItem('pickgok-theme') || 'dark';
            document.documentElement.setAttribute('data-theme', savedTheme);
        })();
    </script>
</head>
<body>

    <div class="mypage-container">
        <div class="mypage-header">
            <a href="${pageContext.request.contextPath}/home" class="logo-link" title="홈으로 돌아가기">
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="PickGok">
            </a>
            <h2 style="margin:0; border:none; font-size:1.5rem;">Settings</h2>
        </div>
        
        <div class="mypage-layout">
            <div class="mypage-sidebar">
                <button class="tab-btn active" onclick="showTab('genre')"><i class="fa-solid fa-music"></i> 선호 장르</button>
                <button class="tab-btn" onclick="showTab('theme')"><i class="fa-solid fa-palette"></i> 화면 모드</button>
                <button class="tab-btn" onclick="showTab('info')"><i class="fa-solid fa-user-gear"></i> 정보 수정</button>
            </div>

            <div class="mypage-content">
                
                <div id="genre" class="section active">
                    <h3>선호 장르 (최대 3개)</h3>
                    <p>저장된 장르: <span style="color:var(--primary-color)"><%= savedGenres %></span></p>
                    
                    <div class="genre-grid">
                        <% String[] genres = {"Hip-Hop", "Pop", "Rock", "Electronic", "Experimental", "Folk", "Instrumental", "International"}; 
                           for(String g : genres) { %>
                            <button class="genre-btn" data-genre="<%= g %>" onclick="toggleGenre(this, '<%= g %>')"><%= g %></button>
                        <% } %>
                    </div>
                    <button class="save-btn" onclick="saveGenres()">변경사항 저장</button>
                </div>

                <div id="theme" class="section">
                    <h3>화면 테마</h3>
                    <div class="theme-card">
                        <div class="theme-option" id="theme-dark" onclick="setTheme('dark')">
                            <i class="fa-solid fa-moon"></i> Dark
                        </div>
                        <div class="theme-option" id="theme-light" onclick="setTheme('light')">
                            <i class="fa-solid fa-sun" style="color: orange;"></i> Light
                        </div>
                    </div>
                </div>

                <div id="info" class="section">
                    <h3>내 정보 수정</h3>
                    <div id="pw-check-step" class="pw-check-area">
                        <p>본인 확인을 위해 비밀번호를 입력하세요.</p>
                        <input type="password" id="check-pw" placeholder="Current Password">
                        <button class="save-btn" onclick="verifyPassword()">확인</button>
                        <p id="pw-msg" style="color: #ff5e57; margin-top: 10px;"></p>
                    </div>

                    <div id="edit-form-step" style="display: none;">
                        <div class="auth-box">
                            <form action="${pageContext.request.contextPath}/mypage" method="post">
                                <label>ID</label>
                                <input type="text" name="userid" value="<%= user.getUserid() %>" readonly>
                                <label>New Password</label>
                                <input type="password" name="password" placeholder="변경 시에만 입력">
                                <label>Name</label>
                                <input type="text" name="name" value="<%= user.getName() %>">
                                <label>Email</label>
                                <input type="email" name="email" value="<%= user.getEmail() %>">
                                <button type="submit" class="save-btn">수정 완료</button>
                            </form>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <script>
        window.contextPath = "${pageContext.request.contextPath}";
        window.savedGenresString = "<%= savedGenres %>";
    </script>
    <script src="${pageContext.request.contextPath}/js/mypage.js"></script>
</body>
</html>