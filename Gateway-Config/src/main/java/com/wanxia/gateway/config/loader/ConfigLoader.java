package com.wanxia.gateway.config.loader;


import com.wanxia.gateway.config.config.Config;
import com.wanxia.gateway.config.util.ConfigUtil;

import static com.wanxia.gateway.common.constant.ConfigConstant.CONFIG_PATH;
import static com.wanxia.gateway.common.constant.ConfigConstant.CONFIG_PREFIX;

/**
 * 配置加载
 */
public class ConfigLoader {

    public static Config load(String[] args) {
        return ConfigUtil.loadConfigFromYaml(CONFIG_PATH, Config.class, CONFIG_PREFIX);
    }

}
