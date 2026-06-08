<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rjgc.nzy.entity.User" %>
<%
    User user = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>知识库管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="/js/jquery-lite.js"></script>
</head>
<body>
<% if (user != null) { %>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="/index">
            <i class="bi bi-journal-text"></i> 文档知识库
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/index"><i class="bi bi-house"></i> 首页</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/knowledge-add"><i class="bi bi-cloud-upload"></i> 上传文档</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/knowledge-list"><i class="bi bi-files"></i> 文档管理</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/ai-qa"><i class="bi bi-robot"></i> AI 问答</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/stats"><i class="bi bi-bar-chart"></i> 数据统计</a>
                </li>
            </ul>
            <span class="navbar-text text-light">
                <i class="bi bi-person-circle"></i> <%= user.getUsername() %>
                <button class="btn btn-outline-light btn-sm ms-2" onclick="logout()">退出</button>
            </span>
        </div>
    </div>
</nav>
<div class="container mt-4">
<% } %>

<script>
function logout() {
    fetch('/api/user/logout', { method: 'POST' }).then(function() {
        window.location.href = '/login';
    });
}
</script>
