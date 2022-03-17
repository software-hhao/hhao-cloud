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


import com.hhao.common.exception.error.server.ServerException;
import com.hhao.common.exception.error.unknow.UnknowException;
import com.hhao.common.springboot.exception.error.other.ResultWrapperException;
import com.hhao.common.springboot.response.ResultWrapper;
import com.hhao.common.springboot.response.ResultWrapperConstant;
import com.hhao.common.springboot.response.ResultWrapperUtil;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;


/**
 * 自定义feign decode
 * 用于解析json格式统一返回的ResultWrapper
 *
 * @author Wang
 * @since 2022/1/8 19:44
 */
public class MyFeignDecoder implements Decoder {
    protected final Logger logger = LoggerFactory.getLogger(MyFeignDecoder.class);

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.body() == null) {
            throw new ServerException();
        }

        try {
            String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
            //对结果进行转换
            ResultWrapper result = ResultWrapperUtil.jsonToResultWrapper(type,bodyStr);

            //如果返回错误，且为内部错误，则直接抛出异常
            if (result.getStatus()!= ResultWrapperConstant.STATUS_SUCCEED) {
                throw new ResultWrapperException(result,response.request().url());
            }
            return result.getData();
        } catch (Exception e) {
            throw new UnknowException(e);
        }
    }
}