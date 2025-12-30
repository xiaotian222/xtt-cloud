/*
 * Copyright 2024 XTT Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xtt.cloud.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * RabbitMQ 消息发送器
 * 
 * 提供便捷的消息发送方法，支持：
 * 1. 发送对象（自动序列化为 JSON）
 * 2. 发送字符串
 * 3. 发送字节数组
 * 4. 支持自定义消息属性（headers、expiration 等）
 * 
 * @author xtt
 */
public class RabbitMessageSender {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMessageSender.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final MessageConverter messageConverter;
    
    public RabbitMessageSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.messageConverter = rabbitTemplate.getMessageConverter();
    }
    
    /**
     * 发送消息到指定 Exchange 和 RoutingKey
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 消息对象（会自动序列化为 JSON）
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("消息发送成功，Exchange: {}, RoutingKey: {}, Message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("消息发送失败，Exchange: {}, RoutingKey: {}, Message: {}", exchange, routingKey, message, e);
            throw new RabbitMQException("消息发送失败", e);
        }
    }
    
    /**
     * 发送消息到指定 Exchange 和 RoutingKey（带消息属性）
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 消息对象
     * @param messageProperties 消息属性
     */
    public void send(String exchange, String routingKey, Object message, MessageProperties messageProperties) {
        try {
            Message amqpMessage = messageConverter.toMessage(message, messageProperties);
            rabbitTemplate.send(exchange, routingKey, amqpMessage);
            log.debug("消息发送成功（带属性），Exchange: {}, RoutingKey: {}, Message: {}", exchange, routingKey, message);
        } catch (Exception e) {
            log.error("消息发送失败（带属性），Exchange: {}, RoutingKey: {}, Message: {}", exchange, routingKey, message, e);
            throw new RabbitMQException("消息发送失败", e);
        }
    }
    
    /**
     * 发送字符串消息
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 字符串消息
     */
    public void sendString(String exchange, String routingKey, String message) {
        send(exchange, routingKey, message);
    }
    
    /**
     * 发送 JSON 字符串消息
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param jsonMessage JSON 字符串
     */
    public void sendJson(String exchange, String routingKey, String jsonMessage) {
        sendString(exchange, routingKey, jsonMessage);
    }
    
    /**
     * 发送对象并转换为 JSON
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param object 要发送的对象
     */
    public void sendObject(String exchange, String routingKey, Object object) {
        send(exchange, routingKey, object);
    }
    
    /**
     * 发送延迟消息
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 消息对象
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendDelayed(String exchange, String routingKey, Object message, long delayMillis) {
        MessageProperties properties = new MessageProperties();
        properties.setDelay((int) delayMillis);
        send(exchange, routingKey, message, properties);
    }
    
    /**
     * 发送带过期时间的消息
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 消息对象
     * @param expirationMillis 过期时间（毫秒）
     */
    public void sendWithExpiration(String exchange, String routingKey, Object message, long expirationMillis) {
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(String.valueOf(expirationMillis));
        send(exchange, routingKey, message, properties);
    }
    
    /**
     * 发送带自定义 Headers 的消息
     * 
     * @param exchange Exchange 名称
     * @param routingKey 路由键
     * @param message 消息对象
     * @param headers 自定义 Headers
     */
    public void sendWithHeaders(String exchange, String routingKey, Object message, java.util.Map<String, Object> headers) {
        MessageProperties properties = new MessageProperties();
        if (headers != null) {
            headers.forEach(properties::setHeader);
        }
        send(exchange, routingKey, message, properties);
    }
    
    /**
     * RabbitMQ 异常
     */
    public static class RabbitMQException extends RuntimeException {
        public RabbitMQException(String message) {
            super(message);
        }
        
        public RabbitMQException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

