package com.hhao.cloud.messagebus;/*
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

import com.hhao.cloud.messagebus.config.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/**
 * BindingServiceProperties
 * BindingService
 * BindingServiceConfiguration
 * RabbitServiceAutoConfiguration
 * BindableProxyFactory
 *
 * @author Wang
 * @since 2022/3/15 15:04
 */
@Component
public class MessagePublish {
    public static StreamBridge streamBridge;
    
    @Autowired
    public void setStreamBridge(StreamBridge streamBridge) {
        MessagePublish.streamBridge = streamBridge;
    }

    public static boolean sendByTopic(String topic,Object data){
        return streamBridge.send(topic + Constants.OUT_TAG + "0",data);
    }

    public static boolean send(String bindingName,Object data){
        return streamBridge.send(bindingName,data);
    }

    public boolean send(String bindingName, Object data, MimeType outputContentType) {
        return streamBridge.send(bindingName,data,outputContentType);
    }

    public boolean send(String bindingName, @Nullable String binderName, Object data) {
        return streamBridge.send(bindingName,binderName,data);
    }

    public boolean send(String bindingName, @Nullable String binderName, Object data, MimeType outputContentType) {
        return streamBridge.send(bindingName,binderName,data,outputContentType);
    }
}
