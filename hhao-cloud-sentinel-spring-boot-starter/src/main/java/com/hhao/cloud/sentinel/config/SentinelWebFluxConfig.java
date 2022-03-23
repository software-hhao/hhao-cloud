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
package com.hhao.cloud.sentinel.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import com.hhao.cloud.sentinel.config.webflux.MyGatewayBlockExceptionHandler;
import com.hhao.cloud.sentinel.config.webflux.MyWebfluxBlockExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Web应用拦截位置：SentinelWebInterceptor
 *
 * @author Wang
 * @since 2022/1/9 9:23
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ServerProperties.class})
@ConditionalOnClass({WebFluxConfigurer.class})
@ConditionalOnMissingBean(SentinelWebFluxConfig.class)
@ConditionalOnProperty(prefix = "spring.cloud.sentinel.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class SentinelWebFluxConfig {

    /**
     * 顺序要在CustomErrorWebExceptionHandler之前
     * @param serverProperties
     * @param serverCodecConfigurer
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(-4)
    @ConditionalOnClass({GatewayCallbackManager.class})
    public MyGatewayBlockExceptionHandler myGatewayBlockExceptionHandler(ServerProperties serverProperties, ServerCodecConfigurer serverCodecConfigurer){
        return new MyGatewayBlockExceptionHandler(serverProperties,serverCodecConfigurer);
    }

    /**
     * 适用于webflux
     * 顺序要在CustomErrorWebExceptionHandler、MyWebfluxBlockExceptionHandler之前
     * @param serverProperties
     * @param serverCodecConfigurer
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(-3)
    @ConditionalOnClass({WebFluxCallbackManager.class})
    public MyWebfluxBlockExceptionHandler myWebfluxBlockExceptionHandler(ServerProperties serverProperties, ServerCodecConfigurer serverCodecConfigurer){
        return new MyWebfluxBlockExceptionHandler(serverProperties,serverCodecConfigurer);
    }
}
