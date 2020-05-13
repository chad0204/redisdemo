package com.pc.cacheloader.constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TVMsg<T> implements Serializable {

    public TVMsg(SinkModel sinkModel, T t) {
        this.sinkModel = sinkModel;
        this.t = t;
    }

    private SinkModel sinkModel;
    private T t;
    private Boolean isSync = false; //同步结果 2位异步结果
}
