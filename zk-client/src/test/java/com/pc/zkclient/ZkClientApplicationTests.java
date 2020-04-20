package com.pc.zkclient;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.recipes.shared.VersionedValue;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;


/**
 *     PERSISTENT(0, false, false)           持久节点：
 *     PERSISTENT_SEQUENTIAL(2, false, true) 顺序持久节点：会给子节点加上序号
 *     EPHEMERAL(1, true, false)             临时节点：
 *     EPHEMERAL_SEQUENTIAL(3, true, true)   顺序临时节点
 *
 *
 *     1.相同路径不能有相同的节点
 *     2.顺序节点会给子节点加序号,一直递增
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZkClientApplicationTests {

    public static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CuratorFramework client;

    @Test
    public void createNode() throws InterruptedException {

        try {
            //临时节点

            //通过get /loader/modify_time命令查看节点
            client.create()
                    .creatingParentsIfNeeded()//父节点不存在则创建父节点。
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath("/ephemeral/modify_time", LocalDateTime.now().format(DATE_TIME_FORMATTER).getBytes());

            //临时顺序节点

            //ls /loader/sequential
            //get /loader/sequential/lock[序号]
            for(int i=0;i<10;i++) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath("/ephemeral/sequential/lock", "lock".getBytes());
            }

            //持久节点,路径已经存在就不能创建
//            client.delete().deletingChildrenIfNeeded().forPath("/persistent/modify_time");//先删除
            client.create()
                    .creatingParentsIfNeeded()//父节点不存在则创建父节点。
                    .forPath("/persistent/modify_time",LocalDateTime.now().format(DATE_TIME_FORMATTER).getBytes());


            for(int i=0;i<10;i++) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                        .forPath("/persistent/sequential/lock", "lock".getBytes());
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeUnit.SECONDS.sleep(1000);
        System.out.println();
    }


    @Test
    public void delNode() throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath("/loader");
    }


    @Test
    public void checkExists() throws Exception {
        if (client.checkExists().forPath("/persistent/sequential/lock") != null) {
            System.out.println(false);
        }
        System.out.println(true);
    }

    @Test
    public void getAndUpdateNodeData() throws Exception {
        byte[] bytes = client.getData().forPath("/persistent/modify_time");
        //更新
        client.setData().forPath("/persistent/modify_time","newValue".getBytes());

        System.out.println(new String(bytes));
    }


    @Test
    public void addListener() throws Exception {
        // 定义了一个启动方法addNodeListener()，监听节点
        String path = "/persistent/modify_time";

        NodeCache nodeCache = new NodeCache(client, path, false);
        try {
            nodeCache.getListenable().addListener(() -> {
                String newData = new String(client.getData().forPath(path));
                if (StringUtils.isEmpty(newData)) {
                    System.out.println("removed "+path);
                }
                System.out.println("changed "+path+":"+newData);
            });
            nodeCache.start(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeUnit.SECONDS.sleep(1000);


        //新增
        client.create().creatingParentsIfNeeded().forPath(path,LocalDateTime.now().toString().getBytes());
        System.out.println("add ");
        TimeUnit.SECONDS.sleep(1);
        //更新
        client.setData().forPath(path,LocalDateTime.now().toString().getBytes());
        System.out.println("update");
        TimeUnit.SECONDS.sleep(1);
        //删除
        client.setData().forPath(path,"".getBytes());
        System.out.println("delete");
        TimeUnit.SECONDS.sleep(1);


        TimeUnit.SECONDS.sleep(1000);

    }

    @Test
    public void lock() throws Exception {
        //独占锁
        InterProcessMutex mutex = new InterProcessMutex(client,"lock");

        mutex.acquire();

        mutex.acquire(100,TimeUnit.MILLISECONDS);

        //信号量
        InterProcessSemaphoreV2 semaphore = new InterProcessSemaphoreV2(client,"semaphore",10);

        //计数器
        SharedCount sharedCount = new SharedCount(client,"count",10);


    }

    @Test
    public void tryLock() {

    }


}
