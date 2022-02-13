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
/**
 * @author Wang
 * @since 2021/12/18 8:56
 */
public interface NacosConfigConstants {
    /**
     * 属性文件前缀
     * The constant PREFIX.
     */
    String PREFIX = "com.hhao.cloud.nacos.config";
    /**
     * 配置路径本地文件夹
     * The constant CONFIG_BASE_FOLDER.
     */
    String CONFIG_BASE_FOLDER="config";
    /**
     * 消息文件本地文件夹
     * The constant MESSAGE_BASE_FOLDER.
     */
    String MESSAGE_BASE_FOLDER="i18n";
    /**
     * 本地文件夹根路径
     * 注意，这个message文件的路径需要配置或采用默认路径，详见MessageSourceConfig
     * The constant SYSTEM_BASE_DIR.
     */
    String SYSTEM_BASE_DIR=System.getProperty("user.dir");

    /**
     * The constant TIMEOUT.
     */
    long TIMEOUT=5000;
}
