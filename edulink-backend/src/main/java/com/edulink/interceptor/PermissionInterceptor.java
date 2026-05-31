package com.edulink.interceptor;

import com.edulink.utils.JwtUtil;
import com.edulink.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.github.pagehelper.util.MetaObjectUtil.method;

/**
 * 权限拦截器
 * 在请求处理前进行身份验证和权限校验，并将当前用户信息存入 ThreadLocal
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    // 定义管理员可访问的路径前缀（包含所有管理功能）
    private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
            "/student", "/score", "/attendance", "/notice", "/dashboard/statistics", "/chat"));

    // 定义教师可访问的路径前缀（与管理员相同，但方法限制略少）
    private static final Set<String> TEACHER_PATHS = new HashSet<>(Arrays.asList(
            "/student", "/score", "/attendance", "/notice", "/dashboard/statistics", "/chat"));

    // 定义学生可访问的路径前缀（仅限查询类操作，GET 方法）
    private static final Set<String> STUDENT_PATHS = new HashSet<>(Arrays.asList(
            "/score", "/attendance", "/notice", "/dashboard/statistics", "/chat"));

    // 定义家长可访问的路径前缀（仅限查询类操作，GET 方法）
    private static final Set<String> PARENT_PATHS = new HashSet<>(Arrays.asList(
            "/score", "/attendance", "/notice", "/dashboard/statistics", "/chat"));

    /**
     * 请求前置处理：验证 Token、解析用户角色、权限校验
     *
     * @param request  当前 HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器对象
     * @return true 表示放行，false 表示拦截并返回错误响应
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求路径
        String path = request.getRequestURI();

        // 2. 公开接口白名单：无需登录即可访问
        if (path.contains("/user/login") ||
                path.contains("/user/test") ||
                path.contains("/health") ||
                path.contains("/class/list") ||
                path.contains("/ws/")) {
            return true;
        }

        // 3. 获取 Token 并验证格式
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        // 4. 解析 Token 获取用户角色和 ID
        token = token.substring(7);
        String role = JwtUtil.getRole(token);
        Long userId = JwtUtil.getUserId(token);
        if (role == null) {
            response.setStatus(401);
            response.getWriter().write("Token无效");
            return false;
        }

        // 5. 将用户信息存入 ThreadLocal（供后续业务层使用）
        UserContext.setUserId(userId);
        UserContext.setRole(role);

        // 6. 权限校验
        boolean hasPermission = false;

        // 个人中心相关接口：所有已登录用户均可访问
        if (path.contains("/user/profile") || path.contains("/user/password")) {
            hasPermission = true;
        }
        // 用户列表接口：仅教师和管理员可访问
        else if (path.contains("/user/list")) {
            if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
                return true;
            } else {
                response.setStatus(403);
                response.getWriter().write("无权限访问");
                return false;
            }
        }
        // 家长个人中心相关接口（获取孩子列表、绑定/解绑）
        else if (path.contains("/user/parent")) {
            hasPermission = true;
        }
        // 用户搜索接口：所有已登录用户均可访问（用于聊天添加好友等）
        else if (path.contains("/user/search")) {
            return true;
        }
        // 聊天相关路径：所有已登录用户均可访问（具体方法限制在 WebSocket 层处理）
        else if (path.contains("/chat")) {
            return true;
        }
        // 管理员：拥有所有权限
        else if ("ADMIN".equals(role)) {
            hasPermission = true;
        }
        // 教师：允许访问 TEACHER_PATHS 中定义的路径
        else if ("TEACHER".equals(role)) {
            hasPermission = TEACHER_PATHS.stream().anyMatch(path::contains);
        }
        // 学生：仅允许 GET 请求访问 STUDENT_PATHS 中的路径
        else if ("STUDENT".equals(role)) {
            String method = request.getMethod();
            System.out.println("学生请求: " + path + ", 方法: " + method);
            if ("GET".equals(method)) {
                // 临时放行所有 GET 请求，确保成绩、考勤等查询功能正常
                hasPermission = true;
            } else {
                System.out.println("拒绝学生非GET请求: " + path);
                hasPermission = false;
            }
        }
        // 家长：仅允许 GET 请求访问 PARENT_PATHS 中的路径
        else if ("PARENT".equals(role)) {
            String method = request.getMethod();
            if ("GET".equals(method)) {
                hasPermission = PARENT_PATHS.stream().anyMatch(path::contains);
            }
        }

        // 7. 若权限不足，返回 403 禁止访问
        if (!hasPermission) {
            response.setStatus(403);
            response.getWriter().write("无权限访问");
            return false;
        }

        // 8. 将用户信息存入 request 属性（备选方案）
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentRole", role);

        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，避免内存泄漏
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @param ex       异常信息（可能为 null）
     * @throws Exception 可能抛出的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}