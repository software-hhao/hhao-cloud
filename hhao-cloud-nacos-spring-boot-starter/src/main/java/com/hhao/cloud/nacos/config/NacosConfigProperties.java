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

package com.hhao.cloud.nacos.config;

import com.alibaba.nacos.api.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * nacos配置新增的配置属性以支持message文件和其它类型文件的配置管理
 *
 * @author Wang
 * @since 2021/12/18 8:59
 */
public class NacosConfigProperties {
    /**
     * 外部配置的文件列表
     */
    private List<Config> extendsionFiles = new ArrayList<>();
    /**
     * 外部配置的message文件
     */
    private List<Config> messages=new ArrayList<>();

    /**
     * Gets extendsion files.
     *
     * @return the extendsion files
     */
    public List<Config> getExtendsionFiles() {
        return extendsionFiles;
    }

    /**
     * Gets messages.
     *
     * @return the messages
     */
    public List<Config> getMessages() {
        return messages;
    }

    /**
     * Sets messages.
     *
     * @param messages the messages
     */
    public void setMessages(List<Config> messages) {
        this.messages = messages;
    }

    /**
     * Sets extendsion files.
     *
     * @param extendsionFiles the extendsion files
     */
    public void setExtendsionFiles(List<Config> extendsionFiles) {
        this.extendsionFiles = extendsionFiles;
    }

    /**
     * The type Config.
     */
    public static class Config {
        private String dataId;
        private String group = Constants.DEFAULT_GROUP;
        private boolean refresh = false;

        /**
         * Gets data id.
         *
         * @return the data id
         */
        public String getDataId() {
            return dataId;
        }

        /**
         * Sets data id.
         *
         * @param dataId the data id
         */
        public void setDataId(String dataId) {
            this.dataId = dataId;
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
         * Is refresh boolean.
         *
         * @return the boolean
         */
        public boolean isRefresh() {
            return refresh;
        }

        /**
         * Sets refresh.
         *
         * @param refresh the refresh
         */
        public void setRefresh(boolean refresh) {
            this.refresh = refresh;
        }
    }
}
