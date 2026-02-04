# literature-archive Backend Foundation

## Overview
Spring Cloud Alibaba microservice scaffold for the 古籍/诗书阅读后台系统. Includes gateway, auth, upload, logging, and monitoring foundations.

## Modules
- common-core: shared response, error codes, trace id filter
- api-gateway: Spring Cloud Gateway + JWT resource server
- user-service: auth server placeholder + RBAC data access
- content-service: content domain placeholder
- asset-service: file upload + object storage client stub
- mapping-service: mapping domain placeholder
- publish-service: publish workflow placeholder
- search-service: elasticsearch integration placeholder
- ops-service: operations placeholder
- chat-service: chat admin placeholder
- monitor-service: metrics/tracing placeholders

## Local configuration
Set environment variables for Nacos, MySQL, ES, and object storage.

```
export NACOS_ADDR=127.0.0.1:8848
export MYSQL_URL=jdbc:mysql://127.0.0.1:3306/iot_cen?useSSL=false&serverTimezone=UTC
export MYSQL_USER=root
export MYSQL_PASSWORD=root
export ES_URIS=http://127.0.0.1:9200
export STORAGE_ENDPOINT=http://127.0.0.1:9000
export STORAGE_ACCESS_KEY=minio
export STORAGE_SECRET_KEY=minio123
export STORAGE_BUCKET=docs
```

## Next steps
- Implement JWT issuance + RBAC in user-service
- Plug in MinIO/S3 SDK for storage client
- Add logging pipeline (Logback + Loki/ELK) and tracing (SkyWalking/Zipkin)
