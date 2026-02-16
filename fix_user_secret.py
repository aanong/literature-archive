
import requests

NACOS_ADDR = "127.0.0.1:8848"
GROUP = "DEFAULT_GROUP"
DATA_ID = "user-service.yaml"

CONTENT = """server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://127.0.0.1:3306/iot_cen?useSSL=false&serverTimezone=UTC}
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
      config:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.literature.user.model
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

security:
  jwt:
    issuer: ${JWT_ISSUER:literature-auth}
    secret: ${JWT_SECRET:5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}
    expire-minutes: ${JWT_EXPIRE_MINUTES:120}

logging:
  level:
    com.literature.user.mapper: DEBUG
"""

def publish():
    url = f"http://{NACOS_ADDR}/nacos/v1/cs/configs"
    data = {
        "dataId": DATA_ID,
        "group": GROUP,
        "content": CONTENT,
        "type": "yaml"
    }
    try:
        response = requests.post(url, data=data)
        print(f"Published {DATA_ID}: {response.status_code}")
    except Exception as e:
        print(e)

if __name__ == "__main__":
    publish()
