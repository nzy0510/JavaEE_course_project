package com.rjgc.nzy.controller;

import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.dto.LoginRequest;
import com.rjgc.nzy.entity.User;
import com.rjgc.nzy.service.CaptchaService;
import com.rjgc.nzy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final CaptchaService captchaService = mock(CaptchaService.class);
    private final UserController controller = new UserController(userService, captchaService);

    @Test
    void loginRejectsInvalidCaptchaBeforeCheckingPassword() {
        LoginRequest request = loginRequest("nzy", "secret", "abcd");
        MockHttpSession session = new MockHttpSession();
        when(captchaService.validate(session, "abcd")).thenReturn(false);

        Result<User> result = controller.login(request, session);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("验证码错误或已过期");
        verify(userService, never()).login("nzy", "secret");
    }

    @Test
    void loginAllowsPasswordCheckWhenCaptchaIsValid() {
        LoginRequest request = loginRequest("nzy", "secret", "a8k2");
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setUsername("nzy");
        user.setPassword("hashed");
        when(captchaService.validate(session, "a8k2")).thenReturn(true);
        when(userService.login("nzy", "secret")).thenReturn(user);

        Result<User> result = controller.login(request, session);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getPassword()).isNull();
        assertThat(session.getAttribute("user")).isSameAs(user);
    }

    private LoginRequest loginRequest(String username, String password, String captcha) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setCaptcha(captcha);
        return request;
    }
}
