#!/bin/bash

# Default to literature-dev if no argument provided
NAMESPACE="${1-literature-dev}"

echo "Using Namespace: '${NAMESPACE}'"

NACOS_ADDR="localhost:8848"
GROUP="DEFAULT_GROUP"

# Login to get access token
LOGIN_RESPONSE=$(curl -s -X POST "http://$NACOS_ADDR/nacos/v1/auth/login" -d "username=nacos&password=nacos")
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "Login failed! Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "Logged in successfully. Access token: $ACCESS_TOKEN"

# Function to publish config
publish_config() {
    local data_id=$1
    local file_path=$2
    
    echo "Publishing $data_id to '$NAMESPACE'..."
    
    curl -X POST "http://$NACOS_ADDR/nacos/v1/cs/configs" \
        -d "dataId=$data_id" \
        -d "group=$GROUP" \
        --data-urlencode "content@$file_path" \
        -d "type=yaml" \
        -d "tenant=$NAMESPACE" \
        -d "accessToken=$ACCESS_TOKEN"

    echo -e "\n"
}

# Publish all configs
publish_config "literature-common.yaml" "nacos_configs/literature-common.yaml"
publish_config "user-service.yaml" "nacos_configs/user-service.yaml"
publish_config "content-service.yaml" "nacos_configs/content-service.yaml"
publish_config "knowledge-service.yaml" "nacos_configs/knowledge-service.yaml"
publish_config "chat-service.yaml" "nacos_configs/chat-service.yaml"
publish_config "asset-service.yaml" "nacos_configs/asset-service.yaml"
publish_config "api-gateway.yaml" "nacos_configs/api-gateway.yaml"

echo "All configs published successfully to '$NAMESPACE'!"
