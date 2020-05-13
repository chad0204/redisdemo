package com.pc.cacheloader.controller;

import com.pc.cacheloader.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author dongxie
 * @date 18:23 2020-05-08
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    UserMapper userMapper;


    @RequestMapping("/getUser")
    public String GetUser(Long id){
        return userMapper.getUser(id,null).toString();
    }


}
