<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<jsp:forward page="/views/home.jsp"></jsp:forward>
<%
    // 접속 시 바로 HomeServlet으로 이동 (/home)
    // HomeServlet이 내부적으로 /views/home.jsp로 포워딩함
    
%>