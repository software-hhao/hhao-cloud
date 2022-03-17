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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * The type Message bus properties.
 *
 * @author Wang
 * @since 2022 /3/16 11:28
 */
@ConfigurationProperties(prefix = "com.hhao.config.message-bus")
public class MessageBusProperties {
    private FunctionPropertie function;
    private List<TopicProperty> topics;

    /**
     * Gets function.
     *
     * @return the function
     */
    public FunctionPropertie getFunction() {
        return function;
    }

    /**
     * Sets function.
     *
     * @param function the function
     */
    public void setFunction(FunctionPropertie function) {
        this.function = function;
    }

    /**
     * Gets topics.
     *
     * @return the topics
     */
    public List<TopicProperty> getTopics() {
        return topics;
    }

    /**
     * Sets topics.
     *
     * @param topics the topics
     */
    public void setTopics(List<TopicProperty> topics) {
        this.topics = topics;
    }

    /**
     * The type Function propertie.
     */
    public static class FunctionPropertie{
        private String definition;

        /**
         * 函数定义
         *
         * @return the definition
         */
        public String getDefinition() {
            return definition;
        }

        /**
         * Sets definition.
         *
         * @param definition the definition
         */
        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }

    /**
     * The type Topic property.
     */
    public static class TopicProperty{
        /**
         * topic名称
         */
        private String name;
        /**
         * 消费者
         */
        private Consumer consumer;
        /**
         * 生产者
         */
        private Provider provider;
        /**
         * 是否生成一个默认的生产者
         */
        private Boolean initDefaultProvider=false;

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets name.
         *
         * @param name the name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets consumer.
         *
         * @return the consumer
         */
        public Consumer getConsumer() {
            return consumer;
        }

        /**
         * Sets consumer.
         *
         * @param consumer the consumer
         */
        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        /**
         * Gets provider.
         *
         * @return the provider
         */
        public Provider getProvider() {
            return provider;
        }

        /**
         * Sets provider.
         *
         * @param provider the provider
         */
        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        /**
         * Gets init default provider.
         *
         * @return the init default provider
         */
        public Boolean getInitDefaultProvider() {
            return initDefaultProvider;
        }

        /**
         * Sets init default provider.
         *
         * @param initDefaultProvider the init default provider
         */
        public void setInitDefaultProvider(Boolean initDefaultProvider) {
            this.initDefaultProvider = initDefaultProvider;
        }

        /**
         * Get default binding name string.
         *
         * @return the string
         */
        public String getDefaultBindingName(){
            return this.getName() + Constants.OUT_TAG + "0";
        }

        /**
         * The type Consumer.
         */
        public static class Consumer{
            /**
             * 对应函数名称
             */
            private String func;
            /**
             * 对应的index
             */
            private Integer index=0;
            /**
             * 组名称
             */
            private String group;
            /**
             * 是否自动生成死信
             */
            private Boolean autoBindDlq=false;
            /**
             * ttl
             */
            private Integer ttl=-1;
            /**
             * 最大尝试次数
             */
            private Integer maxAttempts=1;

            /**
             * Gets func.
             *
             * @return the func
             */
            public String getFunc() {
                return func;
            }

            /**
             * Sets func.
             *
             * @param func the func
             */
            public void setFunc(String func) {
                this.func = func;
            }

            /**
             * Gets index.
             *
             * @return the index
             */
            public Integer getIndex() {
                return index;
            }

            /**
             * Sets index.
             *
             * @param index the index
             */
            public void setIndex(Integer index) {
                this.index = index;
            }

            /**
             * Gets group.
             *
             * @return the group
             */
            public String getGroup() {
                return group;
            }

            /**
             * Sets group.
             *
             * @param group the group
             */
            public void setGroup(String group) {
                this.group = group;
            }

            /**
             * Gets auto bind dlq.
             *
             * @return the auto bind dlq
             */
            public Boolean getAutoBindDlq() {
                return autoBindDlq;
            }

            /**
             * Sets auto bind dlq.
             *
             * @param autoBindDlq the auto bind dlq
             */
            public void setAutoBindDlq(Boolean autoBindDlq) {
                this.autoBindDlq = autoBindDlq;
            }

            /**
             * Gets ttl.
             *
             * @return the ttl
             */
            public Integer getTtl() {
                return ttl;
            }

            /**
             * Sets ttl.
             *
             * @param ttl the ttl
             */
            public void setTtl(Integer ttl) {
                this.ttl = ttl;
            }

            /**
             * Gets max attempts.
             *
             * @return the max attempts
             */
            public Integer getMaxAttempts() {
                return maxAttempts;
            }

            /**
             * Sets max attempts.
             *
             * @param maxAttempts the max attempts
             */
            public void setMaxAttempts(Integer maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            /**
             * Get binding name string.
             *
             * @return the string
             */
            public String getBindingName(){
                return this.getFunc()+Constants.IN_TAG+this.getIndex();
            }
        }

        /**
         * The type Provider.
         */
        public static class Provider{
            private String func;
            private Integer index=0;

            /**
             * Gets func.
             *
             * @return the func
             */
            public String getFunc() {
                return func;
            }

            /**
             * Sets func.
             *
             * @param func the func
             */
            public void setFunc(String func) {
                this.func = func;
            }

            /**
             * Gets index.
             *
             * @return the index
             */
            public Integer getIndex() {
                return index;
            }

            /**
             * Sets index.
             *
             * @param index the index
             */
            public void setIndex(Integer index) {
                this.index = index;
            }

            /**
             * Get binding name string.
             *
             * @return the string
             */
            public String getBindingName(){
                return this.getFunc()+Constants.OUT_TAG+this.getIndex();
            }
        }
    }
}
