package com.rjgc.nzy.controller;

import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.dto.LoginRequest;
import com.rjgc.nzy.dto.RegisterRequest;
import com.rjgc.nzy.entity.User;
import com.rjgc.nzy.service.CaptchaService;
import com.rjgc.nzy.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        ImageIO.write(captchaService.createImage(session), "png", response.getOutputStream());
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request.getUsername(), request.getPassword());
            return Result.ok("注册成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            if (!captchaService.validate(session, request.getCaptcha())) {
                return Result.error(400, "验证码错误或已过期");
            }
            User user = userService.login(request.getUsername(), request.getPassword());
            user.setPassword(null);
            session.setAttribute("user", user);
            return Result.ok(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<User> current(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error(401, "未登录");
        }
        return Result.ok(user);
    }
}
