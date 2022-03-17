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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Nacos配置增加message文件配置管理、其它下载型文件的配置管理
 *
 * @author Wang
 * @since 2021/12/18 9:00
 */
@Configuration
@ConditionalOnClass({com.alibaba.nacos.api.config.ConfigService.class})
@ConditionalOnMissingBean(NacosExtendFileConfig.class)
@ConditionalOnProperty(prefix = "spring.cloud.nacos.config",name = "enabled" ,havingValue = "true",matchIfMissing = true)
public class NacosExtendFileConfig {
    /**
     * The Logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(NacosExtendFileConfig.class);

    /**
     * nacos地址
     */
    private String serverAddr;

    /**
     * 命名空间
     */
    private String namespace;
    private NacosConfigProperties nacosConfigProperties;
    private ConfigService configService = null;
    private ConfigurableApplicationContext applicationContext;

    /**
     * Instantiates a new Nacos message properties file config.
     *
     * @param applicationContext the application context
     */
    @Autowired
    public NacosExtendFileConfig(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        init();
    }

    /**
     * 构建属性文件
     *
     * @param environment
     * @return
     */
    private NacosConfigProperties buildNacosConfigProperties(ConfigurableEnvironment environment) {
        NacosConfigProperties nacosConfigProperties = new NacosConfigProperties();
        Binder binder = Binder.get(environment);
        ResolvableType type = ResolvableType.forClass(NacosConfigProperties.class);
        Bindable<?> target = Bindable.of(type).withExistingValue(nacosConfigProperties);
        binder.bind(NacosConfigConstants.PREFIX, target);
        return nacosConfigProperties;
    }


    private void init() {
        nacosConfigProperties=buildNacosConfigProperties(applicationContext.getEnvironment());
        if (CollectionUtils.isEmpty(nacosConfigProperties.getExtendsionFiles())  && CollectionUtils.isEmpty(nacosConfigProperties.getMessages())){
            return;
        }
        serverAddr = applicationContext.getEnvironment().getProperty("spring.cloud.nacos.config.server-addr");
        namespace = applicationContext.getEnvironment().getProperty("spring.cloud.nacos.config.namespace");

        if (!StringUtils.hasLength(serverAddr)) {
            logger.warn("no define nacos address");
            return;
        }
        if (!StringUtils.hasLength(namespace)) {
            logger.warn("no define nacos namespace");
            namespace = Constants.DEFAULT_NAMESPACE_ID;
        }

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            logger.error("connect error: {}", e);
            return;
        }

        initMessageSource();
        initExtendFiles();
    }

    /**
     * 外部message文件配置
     */
    private void initMessageSource(){
        List<NacosConfigProperties.Config> configs=nacosConfigProperties.getMessages();
        for(NacosConfigProperties.Config config:configs){
            doMessageSource(config,null);
            doMessageSource(config, Locale.CHINA);
            doMessageSource(config,Locale.US);
        }
    }

    /**
     * message外部配置文件处理
     *
     * @param config
     * @param locale
     */
    private void doMessageSource(NacosConfigProperties.Config config, Locale locale){
        String dataId=resolverMessageSourceDataId(config.getDataId(),locale);
        String contend=getRemoteConfigContent(dataId,config.getGroup());
        if (!StringUtils.hasLength(contend)){
            return;
        }
        String fileName=NacosConfigConstants.SYSTEM_BASE_DIR + File.separator + NacosConfigConstants.MESSAGE_BASE_FOLDER + File.separator + dataId;
        saveAsFileWriter(fileName,contend);

        if (config.isRefresh()){
            try {
                setListener(dataId,config.getGroup(),fileName);
            } catch (NacosException e) {
                logger.error("set listener error: {}", e);
            }
        }
    }

    /**
     * 根据message basename解析message文件名称
     * @param baseName
     * @param locale
     * @return
     */
    private String resolverMessageSourceDataId(String baseName, Locale locale){
        String dataId="";
        if (locale == null) {
            dataId = baseName + ".properties";
        } else {
            //dataId = messageName + "_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties";
            dataId =baseName + "_" + locale.getLanguage() + ".properties";
        }
        return dataId;
    }

    /**
     * 获取远程配置内容
     * @param dataId
     * @param group
     * @return
     */
    private String getRemoteConfigContent(String dataId,String group) {
        String content = null;
        try {
            content = configService.getConfig(dataId, group, NacosConfigConstants.TIMEOUT);
            if (!StringUtils.hasLength(content)) {
                logger.warn("configuration is empty, the initialization is skipped! dataId:{}", dataId);
                return null;
            }
            logger.info("configure the content:{}", content);
        } catch (Exception e) {
            logger.error("get remote config conent error: {}", e);
        }
        return content;
    }

    /**
     * 保存外部文件
     * @param fileName
     * @param content
     */
    private void saveAsFileWriter(String fileName, String content) {
        try {
            File file=new File(fileName);
            FileUtils.writeStringToFile(file, content, Charset.forName("UTF-8"));
            logger.info("Configuration has been updated! Local file path:{}", fileName);
        } catch (Exception e) {
            logger.error("configuration exception!Local file path:{} Exception information:{}", fileName, e);
        }
    }

    /**
     * 设置监听
     * @param dataId
     * @param group
     * @param fileName
     * @throws com.alibaba.nacos.api.exception.NacosException
     */
    private void setListener(String dataId,String group,String fileName) throws com.alibaba.nacos.api.exception.NacosException {
        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                logger.info("New configuration received! Configure the content:{}", configInfo);
                try {
                    saveAsFileWriter(fileName, configInfo);
                } catch (Exception e) {
                    logger.error("Initialization configuration exception! Exception information{}", e);
                }
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }

    /**
     * 构建外部配置文件
     */
    private void initExtendFiles(){
        List<NacosConfigProperties.Config> configs=nacosConfigProperties.getExtendsionFiles();
        for(NacosConfigProperties.Config config:configs){
            String contend=getRemoteConfigContent(config.getDataId(),config.getGroup());
            if (!StringUtils.hasLength(contend)){
                return;
            }
            String fileName=NacosConfigConstants.SYSTEM_BASE_DIR + File.separator + NacosConfigConstants.CONFIG_BASE_FOLDER + File.separator + config.getDataId();
            saveAsFileWriter(fileName,contend);

            if (config.isRefresh()){
                try {
                    setListener(config.getDataId(),config.getGroup(),fileName);
                } catch (NacosException e) {
                    logger.error("set listener error: {}", e);
                }
            }
        }
    }
}
