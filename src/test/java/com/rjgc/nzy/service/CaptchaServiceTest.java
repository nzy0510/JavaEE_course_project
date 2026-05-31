package com.rjgc.nzy.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class CaptchaServiceTest {

    private final CaptchaService service = new CaptchaService();

    @Test
    void validateAcceptsCodeIgnoringCaseAndConsumesIt() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CaptchaService.SESSION_KEY, "A8K2");

        assertThat(service.validate(session, "a8k2")).isTrue();
        assertThat(session.getAttribute(CaptchaService.SESSION_KEY)).isNull();
        assertThat(service.validate(session, "a8k2")).isFalse();
    }

    @Test
    void validateRejectsBlankOrMissingCode() {
        MockHttpSession session = new MockHttpSession();

        assertThat(service.validate(session, "")).isFalse();
        assertThat(service.validate(session, "abcd")).isFalse();
    }
}
