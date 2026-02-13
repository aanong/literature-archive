package com.literature.transaction.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Seata Feign 拦截器
 * <p>
 * 用于在 Feign 调用时传播全局事务 XID
 */
public class SeataFeignInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeataFeignInterceptor.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String xid = RootContext.getXID();
        if (StringUtils.hasText(xid)) {
            log.debug("Propagating Seata XID via Feign: {}", xid);
            requestTemplate.header(RootContext.KEY_XID, xid);
        }
    }
}
