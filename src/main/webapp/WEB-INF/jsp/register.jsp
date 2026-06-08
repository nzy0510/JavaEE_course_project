<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 面试题库知识库管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f5f5; }
        .register-card { max-width: 400px; margin: 100px auto; }
    </style>
</head>
<body>
<div class="container">
    <div class="card register-card shadow">
        <div class="card-body p-4">
            <h3 class="text-center mb-4">用户注册</h3>
            <form id="registerForm">
                <div class="mb-3">
                    <label for="username" class="form-label">用户名</label>
                    <input type="text" class="form-control" id="username" name="username" required minlength="3">
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="password" name="password" required minlength="6">
                </div>
                <div class="mb-3">
                    <label for="confirmPassword" class="form-label">确认密码</label>
                    <input type="password" class="form-control" id="confirmPassword" required>
                </div>
                <div id="errorMsg" class="alert alert-danger d-none"></div>
                <div id="successMsg" class="alert alert-success d-none"></div>
                <button type="submit" class="btn btn-primary w-100">注册</button>
            </form>
            <div class="text-center mt-3">
                <a href="/login">已有账号？去登录</a>
            </div>
        </div>
    </div>
</div>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="/js/jquery-lite.js"></script>
<script>
$('#registerForm').on('submit', function(e) {
    e.preventDefault();
    if ($('#password').val() !== $('#confirmPassword').val()) {
        $('#errorMsg').removeClass('d-none').text('两次密码输入不一致');
        return;
    }
    $.ajax({
        url: '/api/user/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: $('#username').val(),
            password: $('#password').val()
        }),
        dataType: 'json',
        success: function(res) {
            if (res.code === 200) {
                $('#successMsg').removeClass('d-none').text('注册成功！即将跳转登录...');
                $('#errorMsg').addClass('d-none');
                setTimeout(function() { window.location.href = '/login'; }, 1500);
            } else {
                $('#errorMsg').removeClass('d-none').text(res.message);
                $('#successMsg').addClass('d-none');
            }
        }
    });
});
</script>
</body>
</html>
