package com.pc.zkclient.util;

import com.pc.zkclient.Test;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Slf4j
@Component
public class ZkClientTemplate {

    @Autowired
    private CuratorFramework client;

    @Autowired
    private Test test;

    /**
     * 创建节点
     *
     * @param path       路径
     * @param createMode 节点类型
     * @param data       节点数据
     * @return 是否创建成功
     */
    public boolean createNode(String path, CreateMode createMode, String data) {
        try {
            client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除节点
     *
     * @param path 路径
     * @return 删除结果
     */
    public boolean deleteNode(String path) {
        try {
            client.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除一个节点，并且递归删除其所有的子节点
     *
     * @param path 路径
     * @return 删除结果
     */
    public boolean deleteChildrenIfNeededNode(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断节点是否存在
     *
     * @param path 路径
     * @return true-存在  false-不存在
     */
    public boolean isExistNode(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);

            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * 判断节点是否是持久化节点
     *
     * @param path 路径
     * @return 2-节点不存在  | 1-是持久化 | 0-临时节点
     */
    public int isPersistentNode(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                return 2;
            }
            if (stat.getEphemeralOwner() > 0) {
                return 1;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    /**
     * 获取节点数据
     *
     * @param path 路径
     * @return 节点数据，如果出现异常，返回null
     */
    public String getNodeData(String path) {

        try {
            byte[] bytes = client.getData().forPath(path);
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 注册节点数据变化事件
     *
     * @param path              节点路径
     * @param nodeCacheListener 监听事件
     * @return 注册结果
     */
    public boolean registerWatcherNodeChanged(String path, NodeCacheListener nodeCacheListener) {
        NodeCache nodeCache = new NodeCache(client, path, false);
        try {
            nodeCache.getListenable().addListener(nodeCacheListener);
            nodeCache.start(true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 更新节点数据
     *
     * @param path     路径
     * @param newValue 新的值
     * @return 更新结果
     */
    public boolean updateNodeData(String path, String newValue) {
        //判断节点是否存在
        if (!isExistNode(path)) {
            return false;
        }

        try {
            client.setData().forPath(path, newValue.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 开启事务
     *
     * @return 返回当前事务
     */
    public CuratorTransaction startTransaction() {
        return client.inTransaction();
    }



    //----------------------------------------lock-----------------------------------------//


    public boolean lock(String key) {
        //加锁的方式是创建节点，创建失败则加锁成功，创建失败则加锁失败，putIfAbsent一样
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL).forPath(key, LocalDateTime.now().toString().getBytes());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean release(String key) {
        try {
            client.delete().forPath(key);
        } catch (Exception e) {
            return false;
        }
        return true;
    }



    public boolean mutexLock(InterProcessMutex mutex,long timeOut,TimeUnit timeUnit) {
        try {
            return mutex.acquire(timeOut,timeUnit);
        } catch (Exception e) {
            return false;
        }

    }



    public void mutexRelease(InterProcessMutex mutex) {

        try {
            mutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }









}
