package com.rjgc.nzy.config;

import com.rjgc.nzy.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthInterceptorTest {

    private final AuthInterceptor interceptor = new AuthInterceptor();

    @ParameterizedTest
    @CsvSource({
            "POST,/api/knowledge/documents/import",
            "POST,/api/knowledge/documents/7/archive",
            "POST,/api/knowledge/documents/7/restore",
            "DELETE,/api/knowledge/documents/7"
    })
    void normalUserCannotUseKnowledgeManagementApis(String method, String uri) throws Exception {
        MockHttpServletRequest request = request(method, uri, user("student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"code\":403");
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/api/knowledge/chunks/search",
            "GET,/api/knowledge/stats",
            "GET,/api/knowledge/documents",
            "POST,/api/ai/ask"
    })
    void normalUserCanUseReadOnlyKnowledgeAndAiApis(String method, String uri) throws Exception {
        MockHttpServletRequest request = request(method, uri, user("student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/index",
            "GET,/ai-qa"
    })
    void normalUserCanOpenBasicPages(String method, String uri) throws Exception {
        MockHttpServletRequest request = request(method, uri, user("student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/knowledge-add",
            "GET,/knowledge-list"
    })
    void normalUserCannotOpenKnowledgeManagementPages(String method, String uri) throws Exception {
        MockHttpServletRequest request = request(method, uri, user("student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/index");
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/knowledge-add",
            "GET,/knowledge-list",
            "POST,/api/knowledge/documents/import",
            "POST,/api/knowledge/documents/7/archive",
            "POST,/api/knowledge/documents/7/restore",
            "DELETE,/api/knowledge/documents/7"
    })
    void adminCanUseKnowledgeManagementPagesAndApis(String method, String uri) throws Exception {
        MockHttpServletRequest request = request(method, uri, user("nzy333"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void adminRoleCanUseKnowledgeManagementApis() throws Exception {
        MockHttpServletRequest request = request("DELETE", "/api/knowledge/documents/7",
                new SessionUser("manager", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void missingRoleIsTreatedAsNormalUser() throws Exception {
        MockHttpServletRequest request = request("DELETE", "/api/knowledge/documents/7", user("nzy"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    private MockHttpServletRequest request(String method, String uri, Object user) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.getSession(true).setAttribute("user", user);
        return request;
    }

    private User user(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    private record SessionUser(String username, String role) {
        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
