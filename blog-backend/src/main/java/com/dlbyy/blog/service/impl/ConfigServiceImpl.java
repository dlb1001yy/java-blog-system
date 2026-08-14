package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.Config;
import com.dlbyy.blog.mapper.ConfigMapper;
import com.dlbyy.blog.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public String getByKey(String configKey) {
        Config config = baseMapper.selectOne(
                new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, configKey));
        return config == null ? null : config.getConfigValue();
    }

    @Override
    public void setByKey(String configKey, String configValue, String description) {
        Config existing = baseMapper.selectOne(
                new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, configKey));
        if (existing == null) {
            Config config = new Config();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            baseMapper.insert(config);
        } else {
            existing.setConfigValue(configValue);
            baseMapper.updateById(existing);
        }
    }
}
