<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.pickgok.track.model.TrackDTO"%>
<%@ page import="com.pickgok.user.model.UserDTO"%>

<%
    // 1. 관리자 권한 체크 (비로그인 또는 일반 유저는 접근 불가)
    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
    if (loginUser == null || !"admin".equals(loginUser.getUserid())) {
        response.sendRedirect(request.getContextPath() + "/home");
        return;
    }

    // 2. 데이터 수신
    Integer seedId = (Integer) request.getAttribute("seedId");
    
    // [FIXED] unchecked 경고를 변수 선언에 직접 suppress
    @SuppressWarnings("unchecked")
    List<TrackDTO> recommendations = (List<TrackDTO>) request.getAttribute("recommendations");
%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>PickGok - 알고리즘 디버그</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* 디버그 페이지 전용 스타일 */
        body {
            flex-direction: column;
            align-items: center;
            padding: 40px;
            overflow-y: auto; /* 스크롤 허용 */
        }
        .debug-header {
            margin-bottom: 30px;
            text-align: center;
            border-bottom: 2px solid #333;
            padding-bottom: 20px;
            width: 100%;
            max-width: 1000px;
        }
        .debug-badge {
            background-color: #e74c3c;
            color: white;
            padding: 5px 10px;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: bold;
            vertical-align: middle;
            margin-left: 10px;
        }
        .seed-info {
            color: #2ecc71;
            font-size: 1.2rem;
            margin-bottom: 20px;
        }
        
        /* 결과 테이블 스타일 */
        .result-table {
            width: 100%;
            max-width: 1000px;
            border-collapse: collapse;
            background-color: #1e1e1e;
            box-shadow: 0 4px 15px rgba(0,0,0,0.5);
        }
        .result-table th, .result-table td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid #333;
        }
        .result-table th {
            background-color: #252525;
            color: #aaa;
            font-size: 0.9rem;
        }
        .result-table tr:hover {
            background-color: #2a2a2a;
        }
        .rank-col {
            font-weight: bold;
            color: #2ecc71;
            width: 60px;
            text-align: center;
        }
        .back-link {
            margin-top: 40px;
            padding: 10px 20px;
            border: 1px solid #555;
            color: #ccc;
            text-decoration: none;
            border-radius: 5px;
            transition: 0.3s;
        }
        .back-link:hover {
            background-color: #eee;
            color: #000;
        }
    </style>
</head>
<body>

    <div class="debug-header">
        <h1>🤖 AI 추천 알고리즘 검증 <span class="debug-badge">ADMIN ONLY</span></h1>
        <p style="color: #888; margin-top: 10px;">Python AI 서버가 반환한 유사도 분석 결과를 원본 데이터와 비교합니다.</p>
    </div>

    <% if (seedId != null) { %>
        <div class="seed-info">
            <i class="fa-solid fa-crosshairs"></i> 기준 트랙 ID: <strong><%= seedId %></strong>
        </div>
    <% } else { %>
        <div class="seed-info" style="color: #e67e22;">
            <i class="fa-solid fa-triangle-exclamation"></i> 테스트 데이터가 없습니다. 메인에서 추천을 실행해주세요.
        </div>
    <% } %>

    <% if (recommendations != null && !recommendations.isEmpty()) { %>
        <table class="result-table">
            <thead>
                <tr>
                    <th class="rank-col">Rank</th>
                    <th>Track ID</th>
                    <th>Album Art</th>
                    <th>Title</th>
                    <th>Artist</th>
                    <th>Genre</th>
                    <th>File Path (Check)</th>
                </tr>
            </thead>
            <tbody>
                <% 
                int rank = 1;
                for(TrackDTO track : recommendations) { 
                %>
                <tr>
                    <td class="rank-col"><%= rank++ %></td>
                    <td><%= track.getTrackId() %></td>
                    <td>
                        <div style="width: 40px; height: 40px; background: #333; overflow: hidden; border-radius: 4px;">
                            <img src="${pageContext.request.contextPath}/img/album_cover.jpg" style="width:100%; height:100%; object-fit:cover;">
                        </div>
                    </td>
                    <td style="font-weight: bold; color: #fff;"><%= track.getTitle() %></td>
                    <td><%= track.getArtist() %></td>
                    <td><span style="background:#333; padding: 2px 6px; border-radius: 3px; font-size: 0.8rem;"><%= track.getGenre() %></span></td>
                    <td style="font-family: monospace; color: #888; font-size: 0.85rem;"><%= track.getFilePath() %></td>
                </tr>
                <% } %>
            </tbody>
        </table>
    <% } else { %>
        <div style="margin-top: 50px; text-align: center; color: #ff5e57;">
            <h3><i class="fa-solid fa-circle-xmark"></i> 데이터 없음</h3>
            <p>AI 서버와의 통신 실패 또는 추천 결과가 0건입니다.</p>
            <p>Python 서버 로그를 확인하세요.</p>
        </div>
    <% } %>

    <a href="${pageContext.request.contextPath}/home" class="back-link">
        <i class="fa-solid fa-arrow-left"></i> 메인으로 돌아가기
    </a>

</body>
</html>