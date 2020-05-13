package com.pc.cacheloader.handler;

import lombok.Data;

/**
 *
 */
@Data
public abstract class NodeHandler<T> implements Node {

    private Integer position;

    /**
     * @param pos
     */
    public void setPosition(Integer pos) {
        position = pos;
    }


    public NodeHandler() {

    }

    private NodeHandler<T> next;


    @Override
    public void setNext(Node node) {
        this.next = (NodeHandler) node;
    }

}
