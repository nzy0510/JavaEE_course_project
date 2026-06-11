package com.rjgc.nzy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String ADMIN_USERNAME = "nzy333";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            // AJAX request — return 401
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
                return false;
            }
            // Page request — redirect to login
            response.sendRedirect("/login");
            return false;
        }

        Object user = session.getAttribute("user");
        if (isAdminOnlyRequest(request) && !isAdmin(user)) {
            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                return false;
            }
            response.sendRedirect("/index");
            return false;
        }
        return true;
    }

    private boolean isAdminOnlyRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if ("/knowledge-add".equals(uri) || "/knowledge-list".equals(uri)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method) && "/api/knowledge/documents/import".equals(uri)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method)
                && uri.matches("/api/knowledge/documents/\\d+/(archive|restore)")) {
            return true;
        }
        return "DELETE".equalsIgnoreCase(method) && uri.matches("/api/knowledge/documents/\\d+");
    }

    private boolean isAdmin(Object user) {
        return "ADMIN".equalsIgnoreCase(readStringProperty(user, "getRole"))
                || ADMIN_USERNAME.equals(readStringProperty(user, "getUsername"));
    }

    private String readStringProperty(Object target, String getterName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(getterName);
            Object value = method.invoke(target);
            return value == null ? null : value.toString();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
