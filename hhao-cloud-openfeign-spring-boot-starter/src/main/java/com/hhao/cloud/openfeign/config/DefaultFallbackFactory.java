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

import com.hhao.common.springboot.exception.AbstractBaseRuntimeException;
import com.hhao.common.springboot.exception.entity.other.ResultWrapperException;
import com.hhao.common.springboot.exception.entity.server.ServiceUnavailableException;
import com.hhao.common.springboot.exception.entity.unknow.UnknowException;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * 默认的FallbackFactory处理类
 *
 * @author Wang
 * @since 2022/1/8 19:55
 */
public class DefaultFallbackFactory implements FallbackFactory<Object> {
    protected final Logger logger = LoggerFactory.getLogger(DefaultFallbackFactory.class);

    @Override
    public Object create(Throwable exception) {
        logger.debug("fallback due to: " + exception.getMessage(), exception);

        if (exception instanceof ResultWrapperException){
            //如果是自定义业务类异常,则直接抛出
            throw (ResultWrapperException)exception;
        }else if (exception instanceof FeignException.ServiceUnavailable ||
            exception instanceof RetryableException
        ){
            //如果是降级异常,则抛出系统忙
            throw new ServiceUnavailableException(exception);
        }else if (exception instanceof AbstractBaseRuntimeException){
            throw (RuntimeException)exception;
        }else{
            //其它抛出未知异常
            throw new UnknowException(exception);
        }
    }
}