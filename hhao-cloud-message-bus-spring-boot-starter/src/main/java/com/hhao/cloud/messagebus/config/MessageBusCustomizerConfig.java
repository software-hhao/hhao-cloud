/*
 * Copyright 2020-2021 WangSheng.
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hhao.cloud.messagebus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * @author Wang
 * @since 2022/3/17 11:52
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.message-bus.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class MessageBusCustomizerConfig {
    protected final Logger logger = LoggerFactory.getLogger(MessageBusCustomizerConfig.class);

    /**
     * 消息总线全局拦截器
     *
     * @return
     */
    @Bean
    @GlobalChannelInterceptor(patterns = "*")
    public ChannelInterceptor globalInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                logger.info(message.toString());
                return message;
            }
        };
    }
}
