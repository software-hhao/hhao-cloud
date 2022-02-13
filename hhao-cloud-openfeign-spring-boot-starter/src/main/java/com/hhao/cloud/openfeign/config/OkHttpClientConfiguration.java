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

import feign.RequestInterceptor;
import feign.RequestTemplate;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.httpclient.DefaultOkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.DefaultOkHttpClientFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Enumeration;

/**
 * OkHttp配置
 * 注释部分可以增加request、response拦截
 *
 * @author Wang
 * @since 2022/1/8 19:56
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OkHttpClient.class)
@ConditionalOnMissingBean(OkHttpClientConfiguration.class)
@ConditionalOnProperty(prefix = "spring.cloud.openfeign.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class OkHttpClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClientConnectionPoolFactory connPoolFactory() {
        return new DefaultOkHttpClientConnectionPoolFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient.Builder okHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClientFactory okHttpClientFactory(OkHttpClient.Builder builder) {
        //builder.addInterceptor(new FeignResponseInterceptor());
        return new DefaultOkHttpClientFactory(builder);
    }


//    @Component
//    class FeignRequestInterceptor implements RequestInterceptor {
//        @Override
//        public void apply(RequestTemplate template) {
//            ServletRequestAttributes attributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (attributes!=null){
//                HttpServletRequest request=attributes.getRequest();
//                Enumeration<String> headNameEnumeration=request.getHeaderNames();
//                while(headNameEnumeration.hasMoreElements()){
//                    String headName=headNameEnumeration.nextElement();
//                    System.out.println(request.getHeader(headName));
//                }
//
//                //template.header("sessionId",request.getHeader("sessionId"));
//            }
//        }
//    }
//
//    class FeignResponseInterceptor implements Interceptor {
//
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Request request=chain.request();
//            Response response=chain.proceed(request);
//
//            MediaType mediaType=response.body().contentType();
//            String content=response.body().string();
//            //解析content
//
//            //生成新的response返回
//            return response.newBuilder().body(ResponseBody.create(mediaType,content)).build();
//        }
//    }
}
