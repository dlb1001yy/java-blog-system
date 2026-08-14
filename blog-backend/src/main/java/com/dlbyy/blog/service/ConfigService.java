package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Config;

public interface ConfigService extends IService<Config> {

    /**
     * 根据 configKey 读取配置值
     *
     * @param configKey 配置键
     * @return 配置值，不存在时返回 null
     */
    String getByKey(String configKey);

    /**
     * 写入（存在则更新，不存在则插入）配置项
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param description 配置描述（新增时写入，更新时不覆盖已有描述）
     */
    void setByKey(String configKey, String configValue, String description);
}
