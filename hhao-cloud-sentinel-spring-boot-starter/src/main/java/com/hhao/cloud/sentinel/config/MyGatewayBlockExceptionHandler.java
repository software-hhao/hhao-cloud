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
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 适用于srping cloud gateway
 * 重写SentinelGatewayBlockExceptionHandler
 *
 * 专门处理BlockException，即限流异常
 * 顺序要在自定义Webflux异常处理的CustomErrorWebExceptionHandler之前，参见ExceptionHandlingWebHandler#handle
 * 参考顺序：
 * [org.springframework.web.server.handler.ExceptionHandlingWebHandler$CheckpointInsertingHandler@6b4a1c9,
 * com.hhao.cloud.sentinel.config.MyGatewayBlockExceptionHandler@595184d8,
 * com.hhao.common.sprintboot.webflux.config.exception.CustomErrorWebExceptionHandler@54ae1240,
 * com.alibaba.csp.sentinel.adapter.spring.webflux.exception.SentinelBlockExceptionHandler@390a7532,
 * org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler@1c2b65cc]
 *
 * @author Wang
 * @since 2022/1/24 22:17
 */
public class MyGatewayBlockExceptionHandler extends AbstractReactiveBlockExceptionHandler {

    public MyGatewayBlockExceptionHandler(ServerProperties serverProperties, ServerCodecConfigurer serverCodecConfigurer) {
        super(serverProperties, serverCodecConfigurer);
    }

    @Override
    protected Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }
}
