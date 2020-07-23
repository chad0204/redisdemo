package com.pc.mybatisdemo.service;

import com.pc.mybatisdemo.mapper.UserMapper;
import com.pc.mybatisdemo.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author pengchao
 * @date 22:39 2020-07-13
 */
@Component
public class UserService {

    @Autowired
    private UserMapper userMapper;


    public UserEntity getUser(Long id,String name) {
        return userMapper.getUser(id,name);
    }





}
