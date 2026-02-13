package com.literature.gateway.controller;

import com.literature.common.core.model.ApiResponse;
import com.literature.crypto.autoconfigure.CryptoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 加密管理控制器
 * <p>
 * 提供运行时动态切换加密开关的管理端点。
 * 仅供管理后台 (Admin Portal) 调用。
 * </p>
 */
@RestController
@RequestMapping("/gateway/security/admin")
public class SecurityAdminController {

    private static final Logger log = LoggerFactory.getLogger(SecurityAdminController.class);

    private final CryptoProperties cryptoProperties;

    public SecurityAdminController(CryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    /**
     * 获取当前加密配置状态
     *
     * @return 当前加密配置
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
                "globalEnabled", cryptoProperties.isEnabled(),
                "httpEnabled", cryptoProperties.getHttp().isEnabled(),
                "nettyEnabled", cryptoProperties.getNetty().isEnabled(),
                "algorithm", cryptoProperties.getAlgorithm(),
                "replayWindowSeconds", cryptoProperties.getHttp().getReplayWindowSeconds());
        return ApiResponse.success(status, null);
    }

    /**
     * 切换 HTTP 加密开关
     *
     * @param enabled 是否启用
     * @return 更新结果
     */
    @PutMapping("/http-encryption")
    public ApiResponse<Map<String, Object>> toggleHttpEncryption(@RequestParam boolean enabled) {
        boolean previousState = cryptoProperties.getHttp().isEnabled();
        cryptoProperties.getHttp().setEnabled(enabled);
        log.info("HTTP 加密状态已切换: {} -> {}", previousState, enabled);

        Map<String, Object> result = Map.of(
                "previousState", previousState,
                "currentState", enabled,
                "message", enabled ? "HTTP 加密已启用" : "HTTP 加密已禁用");
        return ApiResponse.success(result, null);
    }

    /**
     * 切换 Netty (聊天) 加密开关
     *
     * @param enabled 是否启用
     * @return 更新结果
     */
    @PutMapping("/netty-encryption")
    public ApiResponse<Map<String, Object>> toggleNettyEncryption(@RequestParam boolean enabled) {
        boolean previousState = cryptoProperties.getNetty().isEnabled();
        cryptoProperties.getNetty().setEnabled(enabled);
        log.info("Netty 加密状态已切换: {} -> {}", previousState, enabled);

        Map<String, Object> result = Map.of(
                "previousState", previousState,
                "currentState", enabled,
                "message", enabled ? "Netty 加密已启用" : "Netty 加密已禁用");
        return ApiResponse.success(result, null);
    }

    /**
     * 切换全局加密开关
     *
     * @param enabled 是否启用
     * @return 更新结果
     */
    @PutMapping("/global-encryption")
    public ApiResponse<Map<String, Object>> toggleGlobalEncryption(@RequestParam boolean enabled) {
        boolean prevGlobal = cryptoProperties.isEnabled();
        boolean prevHttp = cryptoProperties.getHttp().isEnabled();
        boolean prevNetty = cryptoProperties.getNetty().isEnabled();

        cryptoProperties.setEnabled(enabled);
        cryptoProperties.getHttp().setEnabled(enabled);
        cryptoProperties.getNetty().setEnabled(enabled);
        log.info("全局加密状态已切换: global={}, http={}, netty={} -> 全部 {}",
                prevGlobal, prevHttp, prevNetty, enabled);

        Map<String, Object> result = Map.of(
                "previousState", Map.of("global", prevGlobal, "http", prevHttp, "netty", prevNetty),
                "currentState", enabled,
                "message", enabled ? "全局加密已启用" : "全局加密已禁用");
        return ApiResponse.success(result, null);
    }
}
