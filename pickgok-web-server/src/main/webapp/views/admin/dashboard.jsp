<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.pickgok.track.model.TrackDTO"%>
<%
    @SuppressWarnings("unchecked")
    List<TrackDTO> topTracks = (List<TrackDTO>) request.getAttribute("topTracks");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>PickGok Admin</title>
<style>
body {
	background-color: #121212;
	color: white;
	font-family: sans-serif;
	padding: 40px;
}

table {
	width: 100%;
	border-collapse: collapse;
	margin-top: 20px;
	background: #1e1e1e;
}

th, td {
	padding: 15px;
	text-align: left;
	border-bottom: 1px solid #333;
}

th {
	background-color: #333;
	color: #2ecc71;
}

tr:hover {
	background-color: #252525;
}

a {
	color: #2ecc71;
	text-decoration: none;
	font-weight: bold;
}
</style>
</head>
<body>
	<h1>👑 Top 10 인기 차트</h1>
	<p>사용자들이 가장 많이 재생한 음악입니다.</p>

	<table>
		<thead>
			<tr>
				<th>순위</th>
				<th>ID</th>
				<th>제목</th>
				<th>아티스트</th>
				<th>장르</th>
			</tr>
		</thead>
		<tbody>
			<% if (topTracks != null && !topTracks.isEmpty()) { 
                int rank = 1;
                for (TrackDTO t : topTracks) { %>
			<tr>
				<td><%= rank++ %></td>
				<td><%= t.getTrackId() %></td>
				<td><%= t.getTitle() %></td>
				<td><%= t.getArtist() %></td>
				<td><%= t.getGenre() %></td>
			</tr>
			<%  } 
               } else { %>
			<tr>
				<td colspan="5">아직 재생 기록이 없습니다.</td>
			</tr>
			<% } %>
		</tbody>
	</table>
	<br>
	<a href="${pageContext.request.contextPath}/home">← 메인 서비스로 돌아가기</a>
</body>
</html>