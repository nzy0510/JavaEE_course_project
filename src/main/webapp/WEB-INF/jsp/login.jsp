<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 面试题库知识库管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f5f5; }
        .login-card { max-width: 400px; margin: 100px auto; }
        .captcha-img {
            width: 120px;
            height: 42px;
            cursor: pointer;
            border: 1px solid #dee2e6;
            border-radius: 6px;
        }
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
                <div class="mb-3">
                    <label for="captcha" class="form-label">验证码</label>
                    <div class="d-flex gap-2">
                        <input type="text" class="form-control" id="captcha" name="captcha" maxlength="4" required>
                        <img id="captchaImage" class="captcha-img" src="/api/user/captcha" alt="验证码" title="点击刷新验证码">
                    </div>
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
<script src="/js/jquery-lite.js"></script>
<script>
function refreshCaptcha() {
    $('#captchaImage').attr('src', '/api/user/captcha?t=' + Date.now());
    $('#captcha').val('');
}

$('#captchaImage').on('click', refreshCaptcha);

$('#loginForm').on('submit', function(e) {
    e.preventDefault();
    $.ajax({
        url: '/api/user/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: $('#username').val(),
            password: $('#password').val(),
            captcha: $('#captcha').val()
        }),
        dataType: 'json',
        success: function(res) {
            if (res.code === 200) {
                window.location.href = '/index';
            } else {
                $('#errorMsg').removeClass('d-none').text(res.message);
                refreshCaptcha();
            }
        },
        error: function() {
            $('#errorMsg').removeClass('d-none').text('登录失败，请稍后重试');
            refreshCaptcha();
        }
    });
});
</script>
</body>
</html>
