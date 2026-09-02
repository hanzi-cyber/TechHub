package com.techhub.service;

/**
 * 登录态会话服务:把 token 存到 Redis(带过期时间),实现分布式会话与单点登录
 */
public interface ILoginSessionService {

    /**
     * 保存登录态(单点登录:同一 userId 的新 token 会覆盖旧 token,旧会话自动失效)
     *
     * @param userId 用户ID
     * @param token  登录 token
     */
    void saveToken(Long userId, String token);

    /**
     * 校验登录态:请求携带的 token 必须与 Redis 中该用户的活跃 token 一致
     *
     * @param userId 用户ID
     * @param token  请求携带的 token
     * @return 有效返回 true,否则 false
     */
    boolean isValid(Long userId, String token);

    /**
     * 退出登录:删除该用户的登录态,使 token 立即失效
     *
     * @param userId 用户ID
     */
    void removeToken(Long userId);
}
