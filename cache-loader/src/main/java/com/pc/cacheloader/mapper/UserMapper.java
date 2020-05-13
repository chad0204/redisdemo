package com.pc.cacheloader.mapper;

import com.pc.cacheloader.model.UserEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author dongxie
 * @date 15:01 2020-05-08
 */
@Repository
public interface UserMapper extends BaseMapper {

    UserEntity getUser(@Param("id") Long id,@Param("userName") String userName);

//    List<UserEntity> getUsers(@Param("id") Long id, @Param("userName") String userName);

}
