<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Tự động chuyển hướng người dùng sang Controller Home ngay khi truy cập vào thư mục gốc
    response.sendRedirect(request.getContextPath() + "/home");
%>