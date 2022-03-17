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

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 适用于spring mvc
 * 重写sentinel抛出的BlockException执行逻辑
 * 如果被sentinel流控等，则执行该逻辑
 * FlowException:限流
 * DegradeException:熔断
 * ParamFlowException:热点参数限流
 * SystemBlockException:系统规则
 * AuthorityException:授权规则不通过
 *
 * @author Wang
 * @since 2022/1/6 15:52
 */
public class MyWebBlockExceptionHandler implements BlockExceptionHandler {
    protected final Logger logger = LoggerFactory.getLogger(MyWebBlockExceptionHandler.class);
    private ErrorProperties errorProperties;

    @Autowired
    public MyWebBlockExceptionHandler(ServerProperties serverProperties) {
        this.errorProperties=serverProperties.getError();
    }

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        MediaType mediaType = getMediaType(httpServletRequest);
        if (mediaType==null){
            mediaType=MediaType.APPLICATION_JSON;
        }

        Map<String, Object> errorAttributes=new ErrorAttributesBuilder(new ServiceUnavailableException(e),httpServletRequest).build();

        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        String responseInfo="";

        if (mediaType.includes(MediaType.APPLICATION_XML)){
            httpServletResponse.setContentType(MediaType.APPLICATION_XML_VALUE);
            responseInfo= JacksonUtilFactory.getXmlUtil().obj2String(ResultWrapperBuilder.error(errorAttributes));
        }else{
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseInfo=JacksonUtilFactory.getJsonUtil().obj2String(ResultWrapperBuilder.error(errorAttributes));
        }
        logger.debug(responseInfo);
        httpServletResponse.getWriter().write(responseInfo);
    }

    private MediaType getMediaType(ServletRequest request) {
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_JSON_VALUE;
        }
        if (StringUtils.hasText(contentType)) {
            return MediaType.parseMediaType(contentType);
        }
        return null;
    }

    class ErrorAttributesBuilder{
        private HttpServletRequest httpServletRequest;
        private Exception exception;

        public ErrorAttributesBuilder(Exception exception,HttpServletRequest httpServletRequest){
            this.exception=exception;
            this.httpServletRequest=httpServletRequest;
        }

        public Map<String,Object> build(){
            ErrorAttributeOptions options=this.getErrorAttributeOptions(this.httpServletRequest,MediaType.ALL);
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
            errorAttributes.put(ErrorAttributeConstant.STATUS, getStatus(this.httpServletRequest));
            errorAttributes.put(ErrorAttributeConstant.ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());

            errorAttributes.put(ErrorAttributeConstant.EXCEPTION, exception.getClass().getName());
            errorAttributes.put(ErrorAttributeConstant.PATH, httpServletRequest.getRequestURI());
            addMessage(errorAttributes, exception);
            addStackTrace(errorAttributes, exception);
            addErrorCode(errorAttributes,exception);

            return errorAttributes;
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

        protected ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request, MediaType mediaType) {
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

        protected boolean isIncludeMessage(HttpServletRequest request, MediaType produces) {
            switch (errorProperties.getIncludeMessage()) {
                case ALWAYS:
                    return true;
                case ON_PARAM:
                    return getMessageParameter(request);
                default:
                    return false;
            }
        }

        protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType produces) {
            switch (errorProperties.getIncludeStacktrace()) {
                case ALWAYS:
                    return true;
                case ON_PARAM:
                    return getTraceParameter(request);
                default:
                    return false;
            }
        }

        protected boolean getTraceParameter(HttpServletRequest request) {
            return getBooleanParameter(request, ErrorAttributeConstant.TRACE);
        }

        protected boolean getBooleanParameter(HttpServletRequest request, String parameterName) {
            String parameter = request.getParameter(parameterName);
            if (parameter == null) {
                return false;
            }
            return !"false".equalsIgnoreCase(parameter);
        }

        protected boolean getMessageParameter(HttpServletRequest request) {
            return getBooleanParameter(request, ErrorAttributeConstant.MESSAGE);
        }
    }
}

