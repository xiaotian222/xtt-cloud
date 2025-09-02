#!/bin/sh
echo "Nacos auto config started"
datasourceConfig=$(cat ../config/datasource-config.yaml)
gatewayConfig=$(cat ../config/gateway.yaml)
authConfig=$(cat ../config/auth.yaml)
platformConfig=$(cat ../config/platform.yaml)

groupId="xtt-cloud-oa"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=datasource-config.yaml&group=${groupId}&content=${datasourceConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=gateway.yaml&group=${groupId}&content=${gatewayConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=auth.yaml&group=${groupId}&content=${authConfig}"
curl -X POST "nacos-server:8848/nacos/v1/cs/configs" -d "dataId=platform.yaml&group=${groupId}&content=${platformConfig}"

echo "Nacos config pushed successfully finished"