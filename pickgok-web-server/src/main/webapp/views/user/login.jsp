<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
body {
	justify-content: center;
	align-items: center;
}

.auth-box {
	width: 300px;
	padding: 40px;
	background: #222;
	border-radius: 10px;
	text-align: center;
}

input {
	width: 100%;
	padding: 10px;
	margin: 10px 0;
	background: #333;
	border: none;
	color: white;
}

button {
	width: 100%;
	padding: 10px;
	background: #2ecc71;
	border: none;
	cursor: pointer;
}
</style>
</head>
<body>
	<div class="auth-box">
		<h2 style="color: white;">LOGIN</h2>
		<form action="${pageContext.request.contextPath}/login" method="post">
			<input type="text" name="userid" placeholder="아이디" required>
			<input type="password" name="password" placeholder="비밀번호" required>
			<button type="submit">로그인</button>
		</form>
		<br> <a href="join.jsp" style="color: #aaa;">회원가입</a> | <a
			href="${pageContext.request.contextPath}/home" style="color: #aaa;">메인으로</a>
	</div>
</body>
</html>