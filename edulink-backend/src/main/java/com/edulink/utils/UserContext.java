package com.edulink.utils;

/**
 * 用户上下文工具类
 * 基于 ThreadLocal 存储当前请求线程的用户信息（用户ID和角色）
 * 通常在拦截器中设置，在业务层或控制层获取，请求结束后自动清除
 * 避免通过方法参数层层传递用户信息，简化代码
 */
public class UserContext {

    // 存储当前线程的用户ID
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    // 存储当前线程的用户角色（ADMIN/TEACHER/STUDENT/PARENT）
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return 用户ID，若未设置则返回 null
     */
    public static Long getUserId() {
        return currentUserId.get();
    }

    /**
     * 设置当前线程的用户角色
     *
     * @param role 用户角色
     */
    public static void setRole(String role) {
        currentRole.set(role);
    }

    /**
     * 获取当前线程的用户角色
     *
     * @return 用户角色，若未设置则返回 null
     */
    public static String getRole() {
        return currentRole.get();
    }

    /**
     * 清除当前线程的用户信息（避免内存泄漏）
     * 建议在请求完成后（如拦截器的 afterCompletion 方法中）调用
     */
    public static void clear() {
        currentUserId.remove();
        currentRole.remove();
    }
}