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

import com.hhao.cloud.sentinel.config.web.MyWebBlockExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;

/**
 * Web应用拦截位置：SentinelWebInterceptor
 *
 * @author Wang
 * @since 2022/1/9 9:23
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ServerProperties.class})
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnMissingBean(SentinelWebConfig.class)
@ConditionalOnProperty(prefix = "spring.cloud.sentinel.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class SentinelWebConfig {

    @Bean
    @ConditionalOnMissingBean
    public MyWebBlockExceptionHandler myWebBlockExceptionHandler(ServerProperties serverProperties){
        return new MyWebBlockExceptionHandler(serverProperties);
    }
}
