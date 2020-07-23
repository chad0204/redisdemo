package com.pc.mybatisdemo.mapper;

import com.pc.mybatisdemo.model.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 *
 *
 * 1.直接在接口上配置@Mapper
 * 2.在bean上配置包扫描注解@MapperScan("mapper接口包路径")
 * 3.配置类@Mapper和@MapperScan后可以不用配置@Repository
 *
 */
//@Repository
@Mapper
public interface UserMapper {

    UserEntity getUser(@Param("id") Long id, @Param("userName") String userName);

    int updateById(UserEntity userEntity);


}
