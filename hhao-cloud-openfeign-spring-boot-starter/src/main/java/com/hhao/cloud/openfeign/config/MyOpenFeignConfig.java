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

package com.hhao.cloud.openfeign.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * 用于处理统一返回的openfeign调用
 * 包含异常处理
 * 该配置没有设置为全局，需要在使用的时候指定，如：
 *
 * @author Wang
 * @FeignClient(name = "hhao-cloud-nacos-service-demo", configuration = MyOpenFeignConfig.class, fallbackFactory = DefaultFallbackFactory.class)
 * @since 2022 /1/8 20:27
 */
public class MyOpenFeignConfig {
    /**
     * Retryer retryer.
     *
     * @return the retryer
     */
    @Bean
    public Retryer retryer(){
        return  new Retryer.Default();
    }


    /**
     * NONE – no logging, which is the default
     * BASIC – log only the request method, URL and response status
     * HEADERS – log the basic information together with request and response headers
     * FULL – log the body, headers and metadata for both request and response
     *
     * @return the logger . level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Decoder feignDecoder() {
        return new MyFeignDecoder();
    }

    /**
     * Feign error decoder error decoder.
     *
     * @return the error decoder
     */
    @Bean
    public ErrorDecoder feignErrorDecoder(){
        return new MyFeignErrorDecoder();
    }
}
