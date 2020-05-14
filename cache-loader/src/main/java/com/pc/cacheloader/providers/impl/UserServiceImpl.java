package com.pc.cacheloader.providers.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pc.cacheloader.constants.SinkModel;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.loader.biz.UserLoader;
import com.pc.cacheloader.model.UserEntity;
import com.pc.cacheloader.providers.UserService;
import com.pc.cacheloader.runner.AsyncChannelProcess;
import com.pc.cacheloader.runner.LoaderRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.List;

import javax.annotation.Resource;

/**
 *
 * @author dongxie
 * @date 14:50 2020-05-13
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserLoader userLoader;

    @Resource
    @Qualifier("asyncChannelProcess")
    private AsyncChannelProcess<TVMsg> asyncChannelProcess;

    /**
     * 异步更新,外部队列
     * @param userEntity
     * @return
     */
    @Override
    public Long updateCacheById(UserEntity userEntity) {
        asyncChannelProcess.add(new TVMsg(SinkModel.MODIFIED, userEntity), LoaderRunner.crt.incrementAndGet());
        return userEntity.getId();
    }

    /**
     * 同步更新,内部队列
     * @param userEntity
     * @return
     */
    @Override
    public Long syncUpdateCacheById(UserEntity userEntity) {
        userLoader.consumerMsg(new TVMsg(SinkModel.MODIFIED, userEntity, true));
        return userEntity.getId();
    }

    @Override
    public List<UserEntity> findCacheTeamQuery(UserEntity userEntity) {
        return null;
    }
}
