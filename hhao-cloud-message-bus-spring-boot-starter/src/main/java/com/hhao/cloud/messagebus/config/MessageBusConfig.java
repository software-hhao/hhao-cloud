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

package com.hhao.cloud.messagebus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitBindingProperties;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitConsumerProperties;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitExtendedBindingProperties;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamFunctionProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Wang
 * @since 2022/3/16 11:17
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({MessageBusProperties.class,BindingServiceProperties.class, StreamFunctionProperties.class,RabbitExtendedBindingProperties.class})
@AutoConfigureBefore(BindingServiceConfiguration.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(prefix = "spring.cloud.message-bus.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class MessageBusConfig implements BeanPostProcessor, EnvironmentAware {
    protected final Logger logger = LoggerFactory.getLogger(MessageBusConfig.class);

    private final String FUNC_ENVIRONMENT_NAME="spring.cloud.function.definition";
    private StreamFunctionProperties streamFunctionProperties;
    private MessageBusProperties messageBusProperties;
    private BindingServiceProperties bindingServiceProperties;
    private RabbitExtendedBindingProperties rabbitExtendedBindingProperties;
    private Set<String> functionSet=new HashSet<>();

    @Autowired
    public MessageBusConfig(MessageBusProperties messageBusProperties, BindingServiceProperties bindingServiceProperties,StreamFunctionProperties streamFunctionProperties,RabbitExtendedBindingProperties rabbitExtendedBindingProperties){
        this.streamFunctionProperties=streamFunctionProperties;
        this.bindingServiceProperties=bindingServiceProperties;
        this.messageBusProperties=messageBusProperties;
        this.rabbitExtendedBindingProperties=rabbitExtendedBindingProperties;

        if (StringUtils.hasText(streamFunctionProperties.getDefinition())){
            throw new RuntimeException("Please define spring.cloud.stream.function.definition change to define com.hhao.config.message-bus.function.definition;");
        }
        changeBindingServiceProperties(messageBusProperties,bindingServiceProperties,rabbitExtendedBindingProperties);
    }

    private void changeBindingServiceProperties(MessageBusProperties messageBusProperties, BindingServiceProperties bindingServiceProperties,RabbitExtendedBindingProperties rabbitExtendedBindingProperties){
        Map<String, BindingProperties> bindingPropertiesMap=bindingServiceProperties.getBindings();
        List<MessageBusProperties.TopicProperty> topics=messageBusProperties.getTopics();
        for(MessageBusProperties.TopicProperty topic:topics){
            if (StringUtils.hasText(topic.getName())) {
                //初始化consumer
                initConsumer(topic,bindingPropertiesMap);
                //初始化consumer扩展,如死信处理等
                initConsumerExtended(topic,rabbitExtendedBindingProperties);
                //初始化provider
                initProvider(topic,bindingPropertiesMap);

                if (topic.getInitDefaultProvider()) {
                    initDefaultProvider(topic, bindingPropertiesMap);
                }
            }
        }
    }

    private void initConsumer(MessageBusProperties.TopicProperty topic,Map<String, BindingProperties> bindingPropertiesMap){
        if (topic.getConsumer()==null){
            return;
        }
        String bindingName=topic.getConsumer().getBindingName();
        bindingPropertiesMap.put(bindingName, buildConsumeBindingProperties(topic));
        functionSet.add(topic.getConsumer().getFunc());
    }

    private BindingProperties buildConsumeBindingProperties(MessageBusProperties.TopicProperty topic){
        MessageBusProperties.TopicProperty.Consumer consumer=topic.getConsumer();
        BindingProperties  bindingProperties=new BindingProperties();
        ConsumerProperties consumerProperties=new ConsumerProperties();
        bindingProperties.setConsumer(consumerProperties);

        bindingProperties.setDestination(topic.getName());
        if (StringUtils.hasText(consumer.getGroup())) {
            bindingProperties.setGroup(consumer.getGroup());
        }
        //最大错误尝试
        consumerProperties.setMaxAttempts(consumer.getMaxAttempts());
        return bindingProperties;
    }

    private void initProvider(MessageBusProperties.TopicProperty topic,Map<String, BindingProperties> bindingPropertiesMap){
        if (topic.getProvider()==null){
            return;
        }
        String bindingName=topic.getProvider().getBindingName();
        bindingPropertiesMap.put(bindingName, buildProvideBindingProperties(topic));
        functionSet.add(topic.getProvider().getFunc());
    }

    private BindingProperties buildProvideBindingProperties(MessageBusProperties.TopicProperty topic){
        BindingProperties  bindingProperties=new BindingProperties();
        ProducerProperties producer=new ProducerProperties();
        bindingProperties.setProducer(producer);

        bindingProperties.setDestination(topic.getName());
        return bindingProperties;
    }

    private void initDefaultProvider(MessageBusProperties.TopicProperty topic,Map<String, BindingProperties> bindingPropertiesMap){
        String bindingName=topic.getDefaultBindingName();
        bindingPropertiesMap.put(bindingName, buildProvideBindingProperties(topic));
    }

    @Override
    public void setEnvironment(Environment environment) {
        setFunction(messageBusProperties, (ConfigurableEnvironment)environment);
    }

    private void setFunction(MessageBusProperties messageBusProperties,ConfigurableEnvironment environment){
        StringBuffer definition=new StringBuffer();
        for(String func:functionSet){
            definition.append(func);
            definition.append(";");
        }
        if (StringUtils.hasText(environment.getProperty(FUNC_ENVIRONMENT_NAME))){
            definition.append(environment.getProperty(FUNC_ENVIRONMENT_NAME));
        }
        if (StringUtils.hasText(messageBusProperties.getFunction().getDefinition())){
            definition.append(messageBusProperties.getFunction().getDefinition());
        }
        environment.getSystemProperties().put(FUNC_ENVIRONMENT_NAME,definition.toString());
    }

    private void initConsumerExtended(MessageBusProperties.TopicProperty topic,RabbitExtendedBindingProperties rabbitExtendedBindingProperties){
        if (topic.getConsumer()==null){
            return;
        }
        //复制原属性
        Map<String, RabbitBindingProperties> oldRabbitBindingPropertiesMap=rabbitExtendedBindingProperties.getBindings();
        Map<String, RabbitBindingProperties> rabbitBindingPropertiesMap=new HashMap<>();
        for(Map.Entry<String, RabbitBindingProperties> entrySet: oldRabbitBindingPropertiesMap.entrySet()){
            rabbitBindingPropertiesMap.put(entrySet.getKey(), entrySet.getValue());
        }

        MessageBusProperties.TopicProperty.Consumer consumer=topic.getConsumer();
        if (consumer.getAutoBindDlq()){
            RabbitBindingProperties rabbitBindingProperties=new RabbitBindingProperties();
            rabbitBindingProperties.setConsumer(getRabbitConsumerProperties(topic));
            rabbitBindingPropertiesMap.put(consumer.getBindingName(),rabbitBindingProperties);
        }

        rabbitExtendedBindingProperties.setBindings(rabbitBindingPropertiesMap);
    }

    //扩展属性设置
    private RabbitConsumerProperties getRabbitConsumerProperties(MessageBusProperties.TopicProperty topic){
        RabbitConsumerProperties rabbitBindingProperties=new RabbitConsumerProperties();
        MessageBusProperties.TopicProperty.Consumer consumer=topic.getConsumer();
        if (consumer.getAutoBindDlq()) {
            rabbitBindingProperties.setAutoBindDlq(true);
            if (consumer.getTtl()!=-1) {
                rabbitBindingProperties.setDlqTtl(consumer.getTtl());
            }
        }
        return rabbitBindingProperties;
    }
}
