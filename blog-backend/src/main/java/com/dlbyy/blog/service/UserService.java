package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
}
