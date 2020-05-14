package com.pc.cacheloader.providers;

import com.pc.cacheloader.model.UserEntity;
import java.util.List;

/**
 *
 * @author dongxie
 * @date 14:51 2020-05-13
 */
public interface UserService {

    Long updateCacheById(UserEntity userEntity);

    Long syncUpdateCacheById(UserEntity userEntity);

    List<UserEntity> findCacheTeamQuery(UserEntity userEntity);
}
