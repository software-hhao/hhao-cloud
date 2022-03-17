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

import com.hhao.common.springboot.exception.error.other.ResultWrapperException;
import com.hhao.common.springboot.response.ResultWrapper;
import com.hhao.common.springboot.response.ResultWrapperUtil;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * 关于Feign的异常处理流程
 * 如果远程调用返回业务异常或降级处理，则执行ErrorDecoder;如果同时还定义有Fallback,那么下一步还会执行到Fallback;
 * 如果是sentinel流控处理，则不会执行到ErrorDecoder及Fallback,直接执行sentinel的BlockExceptionHandler
 *
 * @author Wang
 * @since 2022/1/6 21:36
 */
public class MyFeignErrorDecoder extends ErrorDecoder.Default {
    protected final Logger logger = LoggerFactory.getLogger(MyFeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception = super.decode(methodKey, response);
        if (exception instanceof RetryableException ||
                exception instanceof FeignException.ServiceUnavailable
        ) {
            return exception;
        }
        //尝试对response body做ResultWrapper转换
        //如果转换成功，说明是业务类异常
        //如果转换失败，则说明有可能是系统的异常
        String bodyStr ="";
        try {
            if (response.body() != null) {
                bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
                //对结果进行转换
                ResultWrapper result = ResultWrapperUtil.jsonToResultWrapper(HashMap.class,bodyStr);
                return new ResultWrapperException(result,response.request().url());
            }
        } catch (Exception e) {
            logger.debug("Json parsing error:" + bodyStr);
        }
        return exception;
    }
}
