#!/bin/sh
echo "Nacos auto config started"
datasourceConfig=$(cat ../config/datasource-config.yaml)
gatewayConfig=$(cat ../config/gateway.yaml)

groupId="oa"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=datasource-config.yaml&group=${groupId}&content=${datasourceConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=gateway.yaml&group=${groupId}&content=${gatewayConfig}"

echo "Nacos config pushed successfully finished"