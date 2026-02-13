package com.literature.transaction.handler;

import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局事务异常处理器
 * <p>
 * 处理 Seata 分布式事务相关异常
 */
@RestControllerAdvice
public class GlobalTransactionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalTransactionExceptionHandler.class);

    /**
     * 处理 Seata 事务异常
     */
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionException(TransactionException e) {
        log.error("Distributed transaction error: {}", e.getMessage(), e);
        
        // 尝试回滚全局事务
        try {
            String xid = GlobalTransactionContext.getCurrentOrCreate().getXid();
            if (xid != null) {
                log.info("Attempting to rollback global transaction: {}", xid);
                GlobalTransactionContext.reload(xid).rollback();
            }
        } catch (Exception rollbackEx) {
            log.error("Failed to rollback global transaction", rollbackEx);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("code", "5001");
        response.put("message", "Distributed transaction failed: " + e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理通用事务运行时异常
     * <p>
     * 捕获事务相关的运行时异常并尝试回滚
     */
    @ExceptionHandler(io.seata.common.exception.FrameworkException.class)
    public ResponseEntity<Map<String, Object>> handleFrameworkException(Exception e) {
        log.error("Seata framework exception: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("code", "5002");
        response.put("message", "Transaction framework error: " + e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
