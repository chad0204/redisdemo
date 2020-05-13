package com.pc.cacheloader.util;

import com.alibaba.dubbo.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class LoaderMonitor {

    @Autowired
    private ZkClientTemplate zkClientTemplate;

    private Map<String, String> monitorMap = new HashMap<>();

    /**
     * @param path
     * @param data
     */
    public void register(String path, String data) {
        if (monitorMap.get(path) != null) {
            //更新数据
            zkClientTemplate.updateNodeData(path, data);
            monitorMap.put(path, data);
            return;
        }

        if (!zkClientTemplate.isExistNode(path)) {
            zkClientTemplate.crateNode(path, CreateMode.PERSISTENT, data);
        }

        zkClientTemplate.registerWatcherNodeChanged(path, () -> {
            String newData = zkClientTemplate.getNodeData(path);
            if (StringUtils.isBlank(newData)) {
                monitorMap.remove(path);
            }
            log.info("monitor cover old data..{}", newData);
            monitorMap.put(path, newData);
        });
        monitorMap.put(path, data);
    }


    /**
     * @param key
     * @return
     */
    public String getMonitorMapValue(String key) {
        if(monitorMap.get(key) == null){
           String value =  zkClientTemplate.getNodeData(key);
           if(!StringUtils.isBlank(value)){
               monitorMap.put(key,value);
               return value;
           }
        }
        return monitorMap.get(key);
    }


}
