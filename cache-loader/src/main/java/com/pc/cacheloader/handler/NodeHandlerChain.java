package com.pc.cacheloader.handler;

import com.pc.cacheloader.constants.ActionType;
import com.pc.cacheloader.loader.AbstractLoader;
import com.pc.cacheloader.model.BaseDO;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class NodeHandlerChain<T extends BaseDO> {

    private AbstractLoader<T> loader;
    private NodeHandlerCacheProxy<T> head;//头节点
    private List<NodeHandlerCacheProxy<T>> nodeHandles = null;//存储handler
    private ActionType[] actionTypes;

    public NodeHandlerChain() {
    }
    public NodeHandlerChain(AbstractLoader<T> loader) {
        this(loader, null);
    }

    public NodeHandlerChain(AbstractLoader<T> loader, ActionType actionType) {
        this.loader = loader;
        this.actionTypes = new ActionType[1];
        actionTypes[0] = actionType;
    }

    public NodeHandlerChain(AbstractLoader<T> loader, ActionType actionType, ActionType actionType1) {
        this.loader = loader;
        this.actionTypes = new ActionType[2];
        actionTypes[0] = actionType;
        actionTypes[1] = actionType1;
    }

    public void addLastNodeHandler(NodeHandlerCacheProxy<T> nodeHandle) {
        if (nodeHandles == null) {
            nodeHandles = new ArrayList<>();
        }
        nodeHandles.add(nodeHandle);
        //初始化头节点
        if (nodeHandles.size() == 1) {
            head = nodeHandle;
        }
        //链入队列
        else {
            nodeHandles.get(nodeHandles.size() - 1 - 1).setNext(nodeHandle);
        }
        //记录索引
        nodeHandle.setPosition(1 << (nodeHandles.size() - 1));
    }


}
