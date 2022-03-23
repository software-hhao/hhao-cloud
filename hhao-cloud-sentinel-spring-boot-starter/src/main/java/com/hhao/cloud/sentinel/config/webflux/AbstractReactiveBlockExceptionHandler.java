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
package com.hhao.cloud.sentinel.config.webflux;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hhao.common.exception.AbstractBaseRuntimeException;
import com.hhao.common.exception.error.server.ServiceUnavailableException;
import com.hhao.common.jackson.JacksonUtilFactory;
import com.hhao.common.springboot.exception.util.ErrorAttributeConstant;
import com.hhao.common.springboot.response.ResultWrapperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Webflux BlockException处理基类
 * @author Wang
 * @since 2022/1/27 20:04
 */
public abstract class AbstractReactiveBlockExceptionHandler implements WebExceptionHandler {
    protected final Logger logger = LoggerFactory.getLogger(MyGatewayBlockExceptionHandler.class);
    protected ErrorProperties errorProperties;
    protected List<HttpMessageReader<?>> messageReaders = Collections.emptyList();
    protected List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();

    @Autowired
    public AbstractReactiveBlockExceptionHandler(ServerProperties serverProperties, ServerCodecConfigurer serverCodecConfigurer) {
        this.errorProperties=serverProperties.getError();
        this.messageReaders=serverCodecConfigurer.getReaders();
        this.messageWriters=serverCodecConfigurer.getWriters();
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        // This exception handler only handles rejection by Sentinel.
        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }
        return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange,ex));
    }

    protected abstract Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable);

    protected Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange, Throwable throwable) {
        MediaType mediaType =getMediaType(exchange.getRequest());
        if (mediaType==null){
            mediaType=MediaType.APPLICATION_JSON;
        }

        ServerRequest request = ServerRequest.create(exchange, this.messageReaders);
        Map<String, Object> errorAttributes=new ErrorAttributesBuilder(new ServiceUnavailableException(throwable),request).build();
        String responseInfo="";

        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        //serverHttpResponse.getHeaders().setAcceptCharset(Arrays.asList(StandardCharsets.UTF_8));
        //serverHttpResponse.getHeaders().add(HttpHeaders.ACCEPT_ENCODING,"UTF-8");

        if (mediaType.includes(MediaType.APPLICATION_XML)){
            serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_XML);
            responseInfo= JacksonUtilFactory.getXmlUtil().obj2String(ResultWrapperBuilder.error(errorAttributes));
        }else{
            serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            responseInfo= JacksonUtilFactory.getJsonUtil().obj2String(ResultWrapperBuilder.error(errorAttributes));
        }

        byte[] datas =responseInfo.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = serverHttpResponse.bufferFactory().wrap(datas);
        return serverHttpResponse.writeWith(Mono.just(buffer));
    }

    private MediaType getMediaType(ServerHttpRequest request) {
        return request.getHeaders().getContentType();
    }

    public class ErrorAttributesBuilder{
        private ServerRequest request;
        private Exception exception;

        public ErrorAttributesBuilder(Exception exception,ServerRequest request){
            this.exception=exception;
            this.request=request;
        }

        public Map<String,Object> build(){
            ErrorAttributeOptions options=this.getErrorAttributeOptions(this.request,MediaType.ALL);
            Map<String,Object> errorAttributes=new LinkedHashMap<>();
            errorAttributes=getErrorAttributes(errorAttributes);

            if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
                errorAttributes.remove(ErrorAttributeConstant.EXCEPTION);
            }
            if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
                errorAttributes.remove(ErrorAttributeConstant.TRACE);
            }
            if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get(ErrorAttributeConstant.MESSAGE) != null) {
                errorAttributes.remove(ErrorAttributeConstant.MESSAGE);
            }
            return errorAttributes;
        }

        protected Map<String, Object> getErrorAttributes(Map<String,Object> errorAttributes) {
            errorAttributes.put(ErrorAttributeConstant.TIMESTAMP, new Date());

            MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                    .from(exception.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
            HttpStatus errorStatus = determineHttpStatus(exception, responseStatusAnnotation);
            errorAttributes.put(ErrorAttributeConstant.STATUS, errorStatus.value());

            errorAttributes.put(ErrorAttributeConstant.ERROR,errorStatus.getReasonPhrase());

            errorAttributes.put(ErrorAttributeConstant.EXCEPTION, exception.getClass().getName());
            errorAttributes.put(ErrorAttributeConstant.PATH, request.path());
            addMessage(errorAttributes, exception);
            addStackTrace(errorAttributes, exception);
            addErrorCode(errorAttributes,exception);

            return errorAttributes;
        }

        private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
            if (error instanceof ResponseStatusException) {
                return ((ResponseStatusException) error).getStatus();
            }
            return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private void addMessage(Map<String, Object> errorAttributes, Throwable exception) {
            errorAttributes.put(ErrorAttributeConstant.MESSAGE, exception.getMessage());
        }

        private void addErrorCode(Map<String, Object> errorAttributes, Throwable exception) {
            if (exception instanceof AbstractBaseRuntimeException) {
                AbstractBaseRuntimeException error = (AbstractBaseRuntimeException) exception;
                errorAttributes.put(ErrorAttributeConstant.ERROR_CODE, error.getErrorInfo().getCode());
            }else{
                Integer status =(Integer) errorAttributes.get(ErrorAttributeConstant.STATUS);
                errorAttributes.put(ErrorAttributeConstant.ERROR_CODE, status!=null?status:999);
            }
        }

        private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
            StringWriter stackTrace = new StringWriter();
            error.printStackTrace(new PrintWriter(stackTrace));
            stackTrace.flush();
            errorAttributes.put(ErrorAttributeConstant.TRACE, stackTrace.toString());
        }

        protected HttpStatus getStatus(HttpServletRequest request) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        protected ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
            ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
            if (errorProperties.isIncludeException()) {
                options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
            }
            if (isIncludeStackTrace(request, mediaType)) {
                options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
            }
            if (isIncludeMessage(request, mediaType)) {
                options = options.including(ErrorAttributeOptions.Include.MESSAGE);
            }
            return options;
        }


        protected boolean isIncludeStackTrace(ServerRequest request, MediaType produces) {
            switch (errorProperties.getIncludeStacktrace()) {
                case ALWAYS:
                    return true;
                case ON_PARAM:
                    return isTraceEnabled(request);
                default:
                    return false;
            }
        }

        protected boolean isTraceEnabled(ServerRequest request) {
            return getBooleanParameter(request, "trace");
        }

        protected boolean isIncludeMessage(ServerRequest request, MediaType produces) {
            switch (errorProperties.getIncludeMessage()) {
                case ALWAYS:
                    return true;
                case ON_PARAM:
                    return isMessageEnabled(request);
                default:
                    return false;
            }
        }

        protected boolean isMessageEnabled(ServerRequest request) {
            return getBooleanParameter(request, "message");
        }

        private boolean getBooleanParameter(ServerRequest request, String parameterName) {
            String parameter = request.queryParam(parameterName).orElse("false");
            return !"false".equalsIgnoreCase(parameter);
        }
    }
}
