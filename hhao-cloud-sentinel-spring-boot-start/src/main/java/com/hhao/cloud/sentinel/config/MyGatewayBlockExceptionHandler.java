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
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hhao.common.jackson.JacksonUtilFactory;
import com.hhao.common.springboot.exception.AbstractBaseRuntimeException;
import com.hhao.common.springboot.exception.entity.server.ServiceUnavailableException;
import com.hhao.common.springboot.exception.util.ErrorAttributeConstant;
import com.hhao.common.springboot.response.ResultWrapperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
