<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 知识库管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f5f5; }
        .login-card { max-width: 400px; margin: 100px auto; }
    </style>
</head>
<body>
<div class="container">
    <div class="card login-card shadow">
        <div class="card-body p-4">
            <h3 class="text-center mb-4">知识库管理系统</h3>
            <form id="loginForm">
                <div class="mb-3">
                    <label for="username" class="form-label">用户名</label>
                    <input type="text" class="form-control" id="username" name="username" required>
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="password" name="password" required>
                </div>
                <div id="errorMsg" class="alert alert-danger d-none"></div>
                <button type="submit" class="btn btn-primary w-100">登录</button>
            </form>
            <div class="text-center mt-3">
                <a href="/register">还没有账号？去注册</a>
            </div>
        </div>
    </div>
</div>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script>
$('#loginForm').on('submit', function(e) {
    e.preventDefault();
    $.ajax({
        url: '/api/user/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: $('#username').val(),
            password: $('#password').val()
        }),
        dataType: 'json',
        success: function(res) {
            if (res.code === 200) {
                window.location.href = '/index';
            } else {
                $('#errorMsg').removeClass('d-none').text(res.message);
            }
        }
    });
});
</script>
</body>
</html>
