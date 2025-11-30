<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="com.pickgok.user.model.UserDTO"%>
<%
    @SuppressWarnings("unchecked")
    List<UserDTO> userList = (List<UserDTO>) request.getAttribute("userList");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원 관리</title>
<style>
body {
	background: #121212;
	color: white;
	padding: 30px;
	text-align: center;
}

table {
	width: 80%;
	margin: 0 auto;
	border-collapse: collapse;
	background: #1e1e1e;
}

th, td {
	padding: 10px;
	border-bottom: 1px solid #333;
}

th {
	color: #2ecc71;
}

button {
	background: #ff5e57;
	border: none;
	color: white;
	padding: 5px 10px;
	cursor: pointer;
}
</style>
</head>
<body>
	<h1>👥 회원 관리</h1>
	<table>
		<tr>
			<th>ID</th>
			<th>이름</th>
			<th>이메일</th>
			<th>관리</th>
		</tr>
		<% if(userList != null) { for(UserDTO u : userList) { %>
		<tr>
			<td><%= u.getUserid() %></td>
			<td><%= u.getName() %></td>
			<td><%= u.getEmail() %></td>
			<td>
				<% if(!"admin".equals(u.getUserid())) { %>
				<form action="members" method="post"
					onsubmit="return confirm('삭제하시겠습니까?');">
					<input type="hidden" name="deleteId" value="<%= u.getUserid() %>">
					<button type="submit">삭제</button>
				</form> <% } else { %> 관리자 <% } %>
			</td>
		</tr>
		<% } } %>
	</table>
	<br>
	<a href="${pageContext.request.contextPath}/home"
		style="color: #2ecc71;">메인으로</a>
</body>
</html>