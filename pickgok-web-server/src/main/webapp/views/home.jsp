<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.pickgok.track.model.TrackDTO"%>
<%@ page import="com.pickgok.user.model.UserDTO"%> 
<%
    // 1. 세션에서 로그인 유저 정보 가져오기
    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");

    // 2. HomeServlet에서 전달받은 트랙 정보 가져오기
    TrackDTO track = (TrackDTO) request.getAttribute("track");
    
    String title = "";
    String artist = "";
    String musicUrl = "";
    String trackId = "";

    if (track != null) {
        title = track.getTitle();
        artist = track.getArtist();
        musicUrl = request.getContextPath() + track.getFilePath(); 
        trackId = String.valueOf(track.getTrackId());
    } else {
        // [수정됨] 무한 리다이렉트 방지! 
        // 데이터가 없을 때는 기본 메시지를 보여주거나, 리다이렉트를 하지 않습니다.
        title = "재생할 곡이 없습니다.";
        artist = "관리자에게 문의하세요.";
        musicUrl = ""; // 빈 값
        trackId = "0";
        
        // response.sendRedirect(...);  <-- 이 줄을 지우거나 주석 처리해야 루프가 멈춥니다.
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>PickGok</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    
    <script>
        (function() {
            const savedTheme = localStorage.getItem('pickgok-theme') || 'dark';
            document.documentElement.setAttribute('data-theme', savedTheme);
        })();
    </script>
</head>
<body>

    <audio id="audioPlayer" src="<%= musicUrl %>"></audio>
    <input type="hidden" id="currentTrackId" value="<%= trackId %>">

    <aside class="sidebar">
        <div class="logo-box">
            <a href="${pageContext.request.contextPath}/home" title="메인으로">
                <img src="${pageContext.request.contextPath}/img/logo.png" alt="PickGok Logo">
            </a>
        </div>
        <ul class="nav-menu">
            <li><a href="#"><i class="fa-solid fa-list-ul"></i> 내 재생 목록</a></li>
            <li><a href="#"><i class="fa-solid fa-calendar-day"></i> 오늘의 추천</a></li>
            
            <%-- [관리자 전용] 디버그 모드 메뉴 --%>
            <% if (loginUser != null && "admin".equals(loginUser.getUserid())) { %>
                <li><a href="${pageContext.request.contextPath}/views/admin/recommend_result.jsp" style="color: #ff5e57;">
                    <i class="fa-solid fa-bug"></i> 디버그 모드</a>
                </li>
            <% } %>
        </ul>
    </aside>

    <main class="main-content">
        
        <nav class="top-auth">
            <% if (loginUser == null) { %>
                <a href="${pageContext.request.contextPath}/views/user/login.jsp">로그인</a>
                <a href="${pageContext.request.contextPath}/views/user/join.jsp">회원가입</a>
            <% } else { %>
                <span class="user-welcome"><%= loginUser.getName() %>님</span>
                <a href="${pageContext.request.contextPath}/mypage">마이페이지</a>
                
                <% if("admin".equals(loginUser.getUserid())) { %>
                    <a href="${pageContext.request.contextPath}/admin/members" class="admin-link">회원관리</a>
                <% } %>
                
                <a href="${pageContext.request.contextPath}/logout" class="logout-btn">로그아웃</a>
            <% } %>
        </nav>

        <div class="stage-area">
            <button class="action-btn btn-x">
                <i class="fa-solid fa-xmark"></i>
            </button>

            <div class="phone-frame">
                <div class="music-info-box">
                    <div class="music-title"><%= title %></div>
                    <div class="artist-name"><%= artist %></div>
                </div>

                <div class="media-area">
                    <img src="${pageContext.request.contextPath}/img/album_cover.jpg" alt="Album Cover" onerror="this.style.backgroundColor='#333';">
                </div>
                <p class="api-note">AI Recommended Track</p>

                <div class="progress-bar">
                    <div class="progress-fill" id="progressFill"></div>
                </div>

                <div class="controls-area">
                    <div class="toggle-wrapper">
                        <span class="toggle-label">Auto Play</span>
                        <label class="switch">
                            <input type="checkbox" id="autoPlayToggle" checked>
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="play-btn" id="playBtn">
                        <i class="fa-solid fa-play"></i>
                    </div>
                    
                    <div style="width: 44px;"></div> </div>
            </div> 
            
            <button class="action-btn btn-heart">
                <i class="fa-regular fa-heart"></i>
            </button>
        </div>
    </main>

    <script>
        window.contextPath = "${pageContext.request.contextPath}";
        window.isLoggedIn = <%= (loginUser != null) %>;
    </script>
    <script src="${pageContext.request.contextPath}/js/home.js"></script>

</body>
</html>