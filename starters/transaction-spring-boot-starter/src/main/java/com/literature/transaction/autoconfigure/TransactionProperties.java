package com.literature.transaction.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式事务配置属性
 */
@ConfigurationProperties(prefix = "literature.transaction")
public class TransactionProperties {

    /**
     * 是否启用分布式事务
     */
    private boolean enabled = true;

    /**
     * Seata 配置
     */
    private SeataProperties seata = new SeataProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SeataProperties getSeata() {
        return seata;
    }

    public void setSeata(SeataProperties seata) {
        this.seata = seata;
    }

    /**
     * Seata 配置
     */
    public static class SeataProperties {

        /**
         * 应用 ID（通常与微服务名一致）
         */
        private String applicationId;

        /**
         * 事务组名称
         */
        private String txServiceGroup = "literature_tx_group";

        /**
         * Seata Server 地址
         */
        private String serverAddr = "localhost:8091";

        /**
         * 是否启用数据源代理
         */
        private boolean enableAutoDataSourceProxy = true;

        /**
         * 数据源代理模式：AT, XA, TCC
         */
        private String dataSourceProxyMode = "AT";

        /**
         * 是否启用 Feign XID 传播
         */
        private boolean enableFeignXidPropagation = true;

        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public String getTxServiceGroup() {
            return txServiceGroup;
        }

        public void setTxServiceGroup(String txServiceGroup) {
            this.txServiceGroup = txServiceGroup;
        }

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public boolean isEnableAutoDataSourceProxy() {
            return enableAutoDataSourceProxy;
        }

        public void setEnableAutoDataSourceProxy(boolean enableAutoDataSourceProxy) {
            this.enableAutoDataSourceProxy = enableAutoDataSourceProxy;
        }

        public String getDataSourceProxyMode() {
            return dataSourceProxyMode;
        }

        public void setDataSourceProxyMode(String dataSourceProxyMode) {
            this.dataSourceProxyMode = dataSourceProxyMode;
        }

        public boolean isEnableFeignXidPropagation() {
            return enableFeignXidPropagation;
        }

        public void setEnableFeignXidPropagation(boolean enableFeignXidPropagation) {
            this.enableFeignXidPropagation = enableFeignXidPropagation;
        }
    }
}
